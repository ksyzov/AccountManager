package me.ksyz.accountmanager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class AccountManager {
  private static AccountManager accountManagerInstance = null;
  private static ExecutorService pool;
  private static Gson gson;
  private static Type type;
  private static Field sessionField;

  private UserAuthentication userAuth;
  private ArrayList<Account> accounts;
  private String password;
  private boolean isNew;

  static {
    pool = Executors.newFixedThreadPool(1);
    gson = new Gson();
    type = new TypeToken<List<Account>>() {
    }.getType();

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
    // The client generates a random UUID for authentication
    UUID uuid = UUID.randomUUID();

    AuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, uuid.toString());
    userAuth = service.createUserAuthentication(Agent.MINECRAFT);
    service.createMinecraftSessionService();

    isNew = !new File(Minecraft.getMinecraft().mcDataDir, "accounts.enc").exists();
  }

  public static AccountManager getInstance() {
    if (accountManagerInstance == null) {
      accountManagerInstance = new AccountManager();
    }

    return accountManagerInstance;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isNew() {
    return isNew;
  }

  public ArrayList<Account> getAccounts() {
    return accounts;
  }

  public Account getAccountToAdd(String username, String password) {
    Account acc = null;
    if ("".equals(password)) {
      acc = new Account(username, "", username, username, "legacy");
    } else {
      acc = new Account(username, password, "", "", "");
    }

    return acc;
  }

  public void login(Account acc) throws AuthenticationException {
    userAuth.logOut();
    if ("legacy".equals(acc.getType())) {
      userAuth.setUsername(acc.getName());
      setSession(acc.getName(), acc.getName(), "0", "legacy");
    } else {
      userAuth.setUsername(acc.getEmail());
      userAuth.setPassword(acc.getPassword());
      userAuth.logIn();

      GameProfile profile = userAuth.getSelectedProfile();
      String type = userAuth.getUserType().getName();
      setSession(profile.getName(), profile.getId().toString(), userAuth.getAuthenticatedToken(), type);

      acc.setName(profile.getName());
      acc.setUuid(profile.getId().toString());
      acc.setType(type);

      try {
        save();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void save() {
    JsonObject json = new JsonObject();
    json.add("accounts", gson.toJsonTree(accounts, type));
    pool.execute(() -> {
      try {
        FileEncryption.encrypt(password, json);
      } catch (Exception e) {
        e.printStackTrace();
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

  private void create() throws Exception {
    accounts = new ArrayList<Account>();
    new File(Minecraft.getMinecraft().mcDataDir, "accounts.enc").createNewFile();
    save();
  }

  private static void setSession(String name, String uuid, String token, String userType) {
    Session session = new Session(name, uuid, token, userType);
    try {
      sessionField.set(Minecraft.getMinecraft(), session);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
