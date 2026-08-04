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

@Mod(modid = "accountmanager", version = "@VERSION@", clientSideOnly = true, acceptedMinecraftVersions = "1.8.9")
public class AccountManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final File file = new File(mc.mcDataDir, "accounts.json");
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static final ArrayList<Account> accounts = new ArrayList<>();
  private static Account currentSessionAccount;

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
  }

  public static void load() {
    accounts.clear();
    currentSessionAccount = null;
    try {
      JsonElement json = new JsonParser().parse(
        new BufferedReader(new FileReader(file))
      );
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
        }
      }
    } catch (FileNotFoundException e) {
      System.err.print("Couldn't find accounts.json!");
    }
    addCurrentSessionAccount();
  }

  public static void save() {
    try {
      JsonArray jsonArray = new JsonArray();
      for (Account account : accounts) {
        if (account == currentSessionAccount) {
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
        jsonArray.add(jsonObject);
      }
      PrintWriter printWriter = new PrintWriter(new FileWriter(file));
      printWriter.println(gson.toJson(jsonArray));
      printWriter.close();
    } catch (IOException e) {
      System.err.print("Couldn't save accounts.json!");
    }
  }

  private static void addCurrentSessionAccount() {
    net.minecraft.util.Session session = SessionManager.get();
    if (session == null || StringUtils.isBlank(session.getUsername())) {
      return;
    }

    Account.AuthenticationType authenticationType =
      net.minecraft.util.Session.Type.LEGACY.equals(session.getSessionType())
        ? Account.AuthenticationType.OFFLINE
        : Account.AuthenticationType.MICROSOFT;
    for (Account account : accounts) {
      if (
        account.getAuthenticationType() == authenticationType
          && session.getUsername().equalsIgnoreCase(account.getUsername())
      ) {
        return;
      }
    }

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
}
