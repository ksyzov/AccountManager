package me.ksyz.accountmanager.auth;

import me.ksyz.accountmanager.account.LegacyAccount;

public class LegacyAuth {
  public static SessionData login(LegacyAccount account) {
    return new SessionData(
      "", "", account.getUsername(), "legacy"
    );
  }
}
