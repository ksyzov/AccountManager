package me.ksyz.accountmanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import me.ksyz.accountmanager.account.Account;
import me.ksyz.accountmanager.account.LegacyAccount;
import me.ksyz.accountmanager.account.MojangAccount;
import me.ksyz.accountmanager.auth.LegacyAuth;
import me.ksyz.accountmanager.auth.MojangAuth;
import me.ksyz.accountmanager.auth.SessionData;
import me.ksyz.accountmanager.utils.FileEncryption;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final ExecutorService pool = Executors.newFixedThreadPool(1);

  private static AccountManager accountManager = null;
  private static Field sessionField;

  private final ArrayList<Account> accounts;
  private String password;

  static {
    try {
      for (Field f : Minecraft.class.getDeclaredFields()) {
        if (f.getType().isAssignableFrom(Session.class)) {
          sessionField = f;
          break;
        }
      }
      sessionField.setAccessible(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public AccountManager() {
    accounts = new ArrayList<>();
    File file = new File(mc.mcDataDir, "accounts.enc");
    if (!file.exists()) {
      try {
        if (file.createNewFile()) {
          save();
        }
      } catch (IOException exception) {
        System.err.println("Couldn't create accounts.enc in " + mc.mcDataDir);
      }
    }
  }

  public static AccountManager getAccountManager() {
    if (accountManager == null) {
      accountManager = new AccountManager();
    }
    return accountManager;
  }

  public void setSession(SessionData sessionData) {
    try {
      sessionField.set(mc, new Session(
        sessionData.getUsername(),
        sessionData.getUuid(),
        sessionData.getAccessToken(),
        sessionData.getUserType()
      ));
    } catch (IllegalAccessException e) {
      System.err.println("Couldn't access session field");
    }
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ArrayList<Account> getAccounts() {
    return accounts;
  }

  public boolean isAccountInList(String username) {
    for (Account acc : accounts) {
      if (acc instanceof LegacyAccount && acc.getUsername().equals(username)) {
        return true;
      } else if (acc instanceof MojangAccount && ((MojangAccount) acc).getEmail().equals(username)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAccountInList(MojangAccount account) {
    for (Account acc : accounts) {
      if (acc instanceof MojangAccount && ((MojangAccount) acc).getEmail().equals(account.getEmail())) {
        return true;
      }
    }
    return false;
  }

  public void login(Account account) throws AuthenticationException {
    if (account instanceof LegacyAccount) {
      setSession(LegacyAuth.login((LegacyAccount) account));
    } else if (account instanceof MojangAccount) {
      setSession(MojangAuth.login((MojangAccount) account));
    }
  }

  public void save() {
    JsonArray array = new JsonArray();
    for (Account account : accounts) {
      if (account instanceof LegacyAccount) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "Legacy");
        json.addProperty("username", account.getUsername());
        array.add(json);
      } else if (account instanceof MojangAccount) {
        JsonObject jsonAccount = new JsonObject();
        jsonAccount.addProperty("type", "Mojang");
        jsonAccount.addProperty("email", ((MojangAccount) account).getEmail());
        jsonAccount.addProperty("password", ((MojangAccount) account).getPassword());
        jsonAccount.addProperty("username", account.getUsername());
        array.add(jsonAccount);
      }
    }
    pool.execute(() -> {
      try {
        FileEncryption.encrypt(password, array);
      } catch (Exception e) {
        System.err.println("Couldn't save the file");
      }
    });
  }

  public void read() {
    JsonArray array = new JsonArray();
    try {
      array = FileEncryption.decrypt(password);
    } catch (Exception e) {
      System.err.println("Couldn't read the file");
    }
    for (JsonElement element : array) {
      JsonObject json = element.getAsJsonObject();
      String type = json.get("type").getAsString();
      if (type.equals("Legacy")) {
        accounts.add(new LegacyAccount(json.get("username").getAsString()));
      } else if (type.equals("Mojang")) {
        accounts.add(new MojangAccount(
          json.get("email").getAsString(),
          json.get("password").getAsString(),
          json.get("username").getAsString()
        ));
      }
    }
  }
}
