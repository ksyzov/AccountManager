package me.ksyz.accountmanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.ksyz.accountmanager.utils.FileEncryption;
import me.ksyz.accountmanager.utils.SystemUtils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final ExecutorService pool = Executors.newFixedThreadPool(1);
  private static final ArrayList<Account> accounts = new ArrayList<>();

  public static ArrayList<Account> getAccounts() {
    return accounts;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean contains(final String search) {
    for (Account account : accounts) {
      final String username = account.getUsername();
      if (username.equals(search)) {
        return true;
      }
      final String email = account.getEmail();
      if (email.equals(search)) {
        return true;
      }
    }
    return false;
  }

  public static void init() {
    final File file = new File(mc.mcDataDir, "accounts.enc");
    if (!file.exists()) {
      try {
        if (file.createNewFile()) {
          save();
        }
      } catch (IOException e) {
        System.err.println("Couldn't create accounts.enc in " + mc.mcDataDir);
      }
    }
  }

  public static void save() {
    final JsonArray array = new JsonArray();
    for (Account account : accounts) {
      final JsonObject jsonAccount = new JsonObject();
      jsonAccount.addProperty("email", account.getEmail());
      jsonAccount.addProperty("password", account.getPassword());
      jsonAccount.addProperty("username", account.getUsername());
      array.add(jsonAccount);
    }
    pool.execute(() -> {
      try {
        FileEncryption.encrypt(SystemUtils.getSystemInfo(), array);
      } catch (Exception e) {
        System.err.println("Couldn't save the file");
      }
    });
  }

  public static void read() {
    final JsonArray array = new JsonArray();
    try {
      array.addAll(FileEncryption.decrypt(SystemUtils.getSystemInfo()));
    } catch (Exception e) {
      System.err.println("Couldn't read the file");
    }
    for (JsonElement element : array) {
      final JsonObject json = element.getAsJsonObject();
      accounts.add(new Account(
        json.get("email").getAsString(),
        json.get("password").getAsString(),
        json.get("username").getAsString()
      ));
    }
  }
}
