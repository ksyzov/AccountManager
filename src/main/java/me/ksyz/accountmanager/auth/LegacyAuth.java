package me.ksyz.accountmanager.auth;

import me.ksyz.accountmanager.account.LegacyAccount;
import net.minecraft.util.Session;

public class LegacyAuth {
  public static Session login(LegacyAccount account) {
    return new Session(
      account.getUsername(), "", "", Session.Type.LEGACY.toString()
    );
  }
}
