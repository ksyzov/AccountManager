package me.ksyz.accountmanager.auth;

public class SessionData {
  private final String accessToken;
  private final String uuid;
  private final String username;
  private final String userType;

  public SessionData(String accessToken, String uuid, String username, String userType) {
    this.accessToken = accessToken;
    this.uuid = uuid;
    this.username = username;
    this.userType = userType;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getUuid() {
    return uuid;
  }

  public String getUsername() {
    return username;
  }

  public String getUserType() {
    return userType;
  }
}
