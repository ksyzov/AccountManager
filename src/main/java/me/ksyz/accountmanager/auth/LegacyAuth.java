package me.ksyz.accountmanager.auth;

public class LegacyAuth {
  public static SessionData login(Account account) {
    return new SessionData(
      "", "", account.getEmail(), "legacy"
    );
  }
}
