package me.ksyz.accountmanager;


import com.google.gson.*;
import me.ksyz.accountmanager.auth.Account;
import me.ksyz.accountmanager.auth.MicrosoftAuth;
import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.utils.SSLUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Mod(modid = "accountmanager", version = "@VERSION@", clientSideOnly = true, acceptedMinecraftVersions = "1.8.9")
public class AccountManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final File file = new File(mc.mcDataDir, "accounts.json");
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static final ArrayList<Account> accounts = new ArrayList<>();
  private static Account currentSessionAccount;
  private static Account initialLauncherSessionAccount;
  private static net.minecraft.util.Session initialLauncherSession;

  @EventHandler
  public static void init(FMLInitializationEvent event) {
    SSLContext ignored = SSLUtils.getSSLContext();
    MinecraftForge.EVENT_BUS.register(new Events());

    if (!file.exists()) {
      try {
        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
          if (file.createNewFile()) {
            System.out.print("Successfully created accounts.json!");
          }
        }
      } catch (IOException e) {
        System.err.print("Couldn't create accounts.json!");
      }
    }

    initialLauncherSession = SessionManager.get();
    restoreLastLoggedInSession();
  }

  public static void load() {
    load(true);
  }

  private static void load(boolean includeCurrentSession) {
    accounts.clear();
    currentSessionAccount = null;
    initialLauncherSessionAccount = null;
    try {
      JsonElement json;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        json = new JsonParser().parse(reader);
      }
      if (json instanceof JsonArray) {
        JsonArray jsonArray = json.getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          String accessToken = Optional.ofNullable(jsonObject.get("accessToken"))
            .map(JsonElement::getAsString).orElse("");
          long accessTokenExpiresAt = Optional.ofNullable(jsonObject.get("accessTokenExpiresAt"))
            .map(JsonElement::getAsLong).orElse(0L);
          if (accessTokenExpiresAt <= 0L) {
            accessTokenExpiresAt = MicrosoftAuth.getAccessTokenExpiration(accessToken);
          }
          accounts.add(new Account(
            Optional.ofNullable(jsonObject.get("refreshToken")).map(JsonElement::getAsString).orElse(""),
            accessToken,
            Optional.ofNullable(jsonObject.get("username")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("unban")).map(JsonElement::getAsLong).orElse(0L),
            Account.AuthenticationType.fromName(
              Optional.ofNullable(jsonObject.get("authenticationType"))
                .map(JsonElement::getAsString).orElse(Account.AuthenticationType.MICROSOFT.name())
            ),
            Optional.ofNullable(jsonObject.get("oauthClientId")).map(JsonElement::getAsString).orElse(""),
            accessTokenExpiresAt,
            Optional.ofNullable(jsonObject.get("lastAuthError")).map(JsonElement::getAsString).orElse("")
          ));
          Account account = accounts.get(accounts.size() - 1);
          account.setLastLoggedIn(
            Optional.ofNullable(jsonObject.get("lastLoggedIn"))
              .map(JsonElement::getAsBoolean).orElse(false)
          );
        }
      }
    } catch (FileNotFoundException e) {
      System.err.print("Couldn't find accounts.json!");
    } catch (IOException e) {
      System.err.print("Couldn't load accounts.json!");
    }
    if (includeCurrentSession && addCurrentSessionAccount()) {
      save();
    }
    addInitialLauncherSessionAccount();
  }

  private static void restoreLastLoggedInSession() {
    load(false);

    Account lastLoggedInAccount = null;
    for (Account account : accounts) {
      if (account.isLastLoggedIn()) {
        lastLoggedInAccount = account;
        break;
      }
    }
    if (lastLoggedInAccount == null) {
      return;
    }

    if (lastLoggedInAccount.getAuthenticationType() == Account.AuthenticationType.OFFLINE) {
      SessionManager.set(new net.minecraft.util.Session(
        lastLoggedInAccount.getUsername(), "", "", net.minecraft.util.Session.Type.LEGACY.toString()
      ));
      return;
    }

    final Account account = lastLoggedInAccount;
    if (!StringUtils.isBlank(account.getAccessToken())) {
      // Apply the saved session immediately so the launcher-provided session
      // cannot remain active while the token is being validated or refreshed.
      // The async login below replaces the empty player ID with the real one.
      SessionManager.set(new net.minecraft.util.Session(
        account.getUsername(), "", account.getAccessToken(), net.minecraft.util.Session.Type.MOJANG.toString()
      ));
    }
    ExecutorService executor = Executors.newSingleThreadExecutor();
    authenticateAccount(account, executor)
      .thenAccept(session -> {
        SessionManager.set(session);
        markLastLoggedIn(account);
        save();
      })
      .whenComplete((ignored, error) -> executor.shutdown());
  }

  private static CompletableFuture<net.minecraft.util.Session> authenticateAccount(
    Account account,
    Executor executor
  ) {
    AtomicReference<String> rotatedRefreshToken = new AtomicReference<>(account.getRefreshToken());
    AtomicReference<String> minecraftAccessToken = new AtomicReference<>(account.getAccessToken());

    return MicrosoftAuth.login(account.getAccessToken(), executor)
      .handle((session, error) -> session)
      .thenComposeAsync(session -> {
        if (session != null) {
          return CompletableFuture.completedFuture(session);
        }
        return MicrosoftAuth.refreshMSAccessTokens(
            account.getRefreshToken(), account.getAuthenticationType(), account.getOauthClientId(), executor
          )
          .thenComposeAsync(msAccessTokens -> {
            rotatedRefreshToken.set(msAccessTokens.get("refresh_token"));
            return MicrosoftAuth.acquireXboxAccessToken(
              msAccessTokens.get("access_token"),
              account.getAuthenticationType(),
              account.getOauthClientId(),
              executor
            );
          }, executor)
          .thenComposeAsync(xboxAccessToken ->
            MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor), executor
          )
          .thenComposeAsync(xboxXstsData ->
            MicrosoftAuth.acquireMCAccessToken(
              xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor
            ), executor
          )
          .thenComposeAsync(mcToken -> {
            minecraftAccessToken.set(mcToken);
            return MicrosoftAuth.login(mcToken, executor);
          }, executor);
      }, executor)
      .thenApply(session -> {
        account.setRefreshToken(rotatedRefreshToken.get());
        account.setAccessToken(minecraftAccessToken.get());
        account.setUsername(session.getUsername());
        account.setAccessTokenExpiresAt(
          MicrosoftAuth.getAccessTokenExpiration(minecraftAccessToken.get())
        );
        account.setLastAuthError("");
        return session;
      });
  }

  public static void save() {
    try {
      JsonArray jsonArray = new JsonArray();
      for (Account account : accounts) {
        if (account == currentSessionAccount || account == initialLauncherSessionAccount) {
          continue;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("refreshToken", account.getRefreshToken());
        jsonObject.addProperty("accessToken", account.getAccessToken());
        jsonObject.addProperty("username", account.getUsername());
        jsonObject.addProperty("unban", account.getUnban());
        jsonObject.addProperty("authenticationType", account.getAuthenticationType().name());
        jsonObject.addProperty("oauthClientId", account.getOauthClientId());
        jsonObject.addProperty("accessTokenExpiresAt", account.getAccessTokenExpiresAt());
        jsonObject.addProperty("lastAuthError", account.getLastAuthError());
        jsonObject.addProperty("lastLoggedIn", account.isLastLoggedIn());
        jsonArray.add(jsonObject);
      }
      PrintWriter printWriter = new PrintWriter(new FileWriter(file));
      printWriter.println(gson.toJson(jsonArray));
      printWriter.close();
    } catch (IOException e) {
      System.err.print("Couldn't save accounts.json!");
    }
  }

  private static boolean addCurrentSessionAccount() {
    net.minecraft.util.Session session = SessionManager.get();
    if (session == null || StringUtils.isBlank(session.getUsername())) {
      return false;
    }

    // A refresh-token account still produces a normal Microsoft session after
    // login. Match the saved account by username instead of by credential type.
    Account existingAccount = findAccountByUsername(session.getUsername());
    if (existingAccount != null) {
      boolean markerChanged = false;
      for (Account account : accounts) {
        boolean shouldBeLastLoggedIn = account == existingAccount;
        if (account.isLastLoggedIn() != shouldBeLastLoggedIn) {
          markerChanged = true;
        }
      }
      markLastLoggedIn(existingAccount);
      return markerChanged;
    }

    Account.AuthenticationType authenticationType =
      net.minecraft.util.Session.Type.LEGACY.equals(session.getSessionType())
        ? Account.AuthenticationType.OFFLINE
        : Account.AuthenticationType.MICROSOFT;
    currentSessionAccount = new Account(
      "",
      session.getToken(),
      session.getUsername(),
      0L,
      authenticationType,
      "",
      MicrosoftAuth.getAccessTokenExpiration(session.getToken()),
      ""
    );
    accounts.add(0, currentSessionAccount);
    return false;
  }

  private static void addInitialLauncherSessionAccount() {
    if (initialLauncherSession == null || StringUtils.isBlank(initialLauncherSession.getUsername())) {
      return;
    }

    Account existingAccount = findAccountByUsername(initialLauncherSession.getUsername());
    if (existingAccount != null) {
      if (existingAccount == currentSessionAccount) {
        initialLauncherSessionAccount = existingAccount;
      }
      return;
    }

    Account.AuthenticationType authenticationType =
      net.minecraft.util.Session.Type.LEGACY.equals(initialLauncherSession.getSessionType())
        ? Account.AuthenticationType.OFFLINE
        : Account.AuthenticationType.MICROSOFT;
    initialLauncherSessionAccount = new Account(
      "",
      initialLauncherSession.getToken(),
      initialLauncherSession.getUsername(),
      0L,
      authenticationType,
      "",
      MicrosoftAuth.getAccessTokenExpiration(initialLauncherSession.getToken()),
      ""
    );
    accounts.add(0, initialLauncherSessionAccount);
  }

  private static Account findAccountByUsername(String username) {
    for (Account account : accounts) {
      if (username.equalsIgnoreCase(account.getUsername())) {
        return account;
      }
    }
    return null;
  }

  public static boolean isLauncherAccount(Account account) {
    if (account == null) {
      return false;
    }
    return account == initialLauncherSessionAccount
      || (initialLauncherSession != null
        && initialLauncherSession.getUsername().equalsIgnoreCase(account.getUsername()));
  }

  public static boolean isCurrentSessionAccount(Account account) {
    if (account == null) {
      return false;
    }
    net.minecraft.util.Session session = SessionManager.get();
    return session != null
      && !StringUtils.isBlank(session.getUsername())
      && session.getUsername().equalsIgnoreCase(account.getUsername());
  }

  public static void switchToLauncherSession() {
    if (initialLauncherSession == null) {
      return;
    }

    SessionManager.set(initialLauncherSession);
    for (Account account : accounts) {
      if (isLauncherAccount(account)) {
        markLastLoggedIn(account);
        return;
      }
    }
  }

  public static Account findDuplicateAccount(Account candidate) {
    return findDuplicateAccount(candidate, null);
  }

  public static Account findDuplicateAccount(Account candidate, Account ignoredAccount) {
    for (Account account : accounts) {
      if (account == ignoredAccount) {
        continue;
      }
      if (account.getAuthenticationType() != candidate.getAuthenticationType()) {
        continue;
      }

      boolean sameUsername = !StringUtils.isBlank(candidate.getUsername())
        && !StringUtils.isBlank(account.getUsername())
        && candidate.getUsername().equalsIgnoreCase(account.getUsername());
      if (sameUsername) {
        return account;
      }
    }
    return null;
  }

  public static void markLastLoggedIn(Account loggedInAccount) {
    for (Account account : accounts) {
      account.setLastLoggedIn(account == loggedInAccount);
    }
  }
}
