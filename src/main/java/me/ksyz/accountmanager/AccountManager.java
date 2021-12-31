package me.ksyz.accountmanager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.exceptions.AuthenticationException;
import me.ksyz.accountmanager.auth.Account;
import me.ksyz.accountmanager.auth.LegacyAuth;
import me.ksyz.accountmanager.auth.MojangAuth;
import me.ksyz.accountmanager.auth.SessionData;
import me.ksyz.accountmanager.utils.FileEncryption;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final ExecutorService pool = Executors.newFixedThreadPool(1);
  private static final Gson gson = new Gson();
  private static final Type type = (
    new TypeToken<List<Account>>() {
    }.getType()
  );

  private static AccountManager accountManager = null;
  private static Field sessionField;

  private final boolean isNew;
  private ArrayList<Account> accounts;
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

  private static void setSession(SessionData sessionData) {
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

  public AccountManager() {
    isNew = !new File(mc.mcDataDir, "accounts.enc").exists();
  }

  public static AccountManager getAccountManager() {
    if (accountManager == null) {
      accountManager = new AccountManager();
    }
    return accountManager;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ArrayList<Account> getAccounts() {
    return accounts;
  }

  public Account getAccountToAdd(String email, String password) {
    Account acc;
    if ("".equals(password)) {
      acc = new Account(email, "", "legacy", email);
    } else {
      acc = new Account(email, password, "mojang", "");
    }
    return acc;
  }

  public void login(Account account) throws AuthenticationException {
    SessionData sessionData;
    if ("legacy".equals(account.getUserType())) {
      sessionData = LegacyAuth.login(account);
      setSession(sessionData);
    } else {
      sessionData = MojangAuth.login(account);
      account.setUserType(sessionData.getUserType());
      account.setUsername(sessionData.getUsername());
    }
    setSession(sessionData);
    save();
  }

  public void create() throws Exception {
    accounts = new ArrayList<>();
    if (new File(mc.mcDataDir, "accounts.enc").createNewFile()) {
      save();
    }
  }

  public void save() {
    JsonObject json = new JsonObject();
    json.add("accounts", gson.toJsonTree(accounts, type));
    pool.execute(() -> {
      try {
        FileEncryption.encrypt(password, json);
      } catch (Exception e) {
        System.err.println("Couldn't save the file");
      }
    });
  }

  public void read() throws Exception {
    if (isNew) {
      create();
      return;
    }
    JsonObject json = FileEncryption.decrypt(password);
    accounts = gson.fromJson(json.getAsJsonArray("accounts"), type);
  }
}
