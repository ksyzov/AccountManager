package me.ksyz.accountmanager.auth;

public class Account {
  public enum AuthenticationType {
    MICROSOFT("Microsoft"),
    REFRESH_TOKEN("Refresh token");

    private final String displayName;

    AuthenticationType(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }

    public static AuthenticationType fromName(String name) {
      try {
        return valueOf(name);
      } catch (Exception ignored) {
        return MICROSOFT;
      }
    }
  }

  private String refreshToken;
  private String accessToken;
  private String username;
  private long unban;
  private AuthenticationType authenticationType;
  private String oauthClientId;
  private long accessTokenExpiresAt;
  private String lastAuthError;

  public Account(String refreshToken, String accessToken, String username) {
    this(refreshToken, accessToken, username, 0L);
  }

  public Account(String refreshToken, String accessToken, String username, long unban) {
    this(refreshToken, accessToken, username, unban, AuthenticationType.MICROSOFT, "", 0L, "");
  }

  public Account(
    String refreshToken,
    String accessToken,
    String username,
    long unban,
    AuthenticationType authenticationType,
    String oauthClientId,
    long accessTokenExpiresAt,
    String lastAuthError
  ) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.username = username;
    this.unban = unban;
    this.authenticationType = authenticationType == null ? AuthenticationType.MICROSOFT : authenticationType;
    this.oauthClientId = oauthClientId == null ? "" : oauthClientId;
    this.accessTokenExpiresAt = accessTokenExpiresAt;
    this.lastAuthError = lastAuthError == null ? "" : lastAuthError;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getUsername() {
    return username;
  }

  public long getUnban() {
    return unban;
  }

  public AuthenticationType getAuthenticationType() {
    return authenticationType == null ? AuthenticationType.MICROSOFT : authenticationType;
  }

  public long getAccessTokenExpiresAt() {
    return accessTokenExpiresAt;
  }

  public String getOauthClientId() {
    return oauthClientId == null ? "" : oauthClientId;
  }

  public String getLastAuthError() {
    return lastAuthError == null ? "" : lastAuthError;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setUnban(long unban) {
    this.unban = unban;
  }

  public void setAuthenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
  }

  public void setOauthClientId(String oauthClientId) {
    this.oauthClientId = oauthClientId == null ? "" : oauthClientId;
  }

  public void setAccessTokenExpiresAt(long accessTokenExpiresAt) {
    this.accessTokenExpiresAt = accessTokenExpiresAt;
  }

  public void setLastAuthError(String lastAuthError) {
    this.lastAuthError = lastAuthError == null ? "" : lastAuthError;
  }
}
