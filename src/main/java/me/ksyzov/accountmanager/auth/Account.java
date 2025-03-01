package me.ksyzov.accountmanager.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

public class Account {
  private String refreshToken;
  private String accessToken;
  private String username;
  private long unban;
  private AccountType type;


  public Account(String refreshToken, String accessToken, String username) {
    this(refreshToken, accessToken, username, 0L, AccountType.PREMIUM);
  }

  public Account(String refreshToken, String accessToken, String username, long unban) {
    this(refreshToken, accessToken, username, unban, AccountType.PREMIUM);
  }


  public Account(String refreshToken, String accessToken, String username, long unban, AccountType type) {
    this.refreshToken = refreshToken;
    this.accessToken = accessToken;
    this.username = username;
    this.unban = unban;
    this.type = type;
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

  public AccountType getType() {
    return type;
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

  public void setType(AccountType type) {
    this.type = type;
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("refreshToken", refreshToken);
    jsonObject.addProperty("accessToken", accessToken);
    jsonObject.addProperty("username", username);
    jsonObject.addProperty("unban", unban);
    jsonObject.addProperty("type", type.toString()); // Save the account type
    return jsonObject;
  }

  public static Account fromJson(JsonObject jsonObject) {
    return new Account(
            Optional.ofNullable(jsonObject.get("refreshToken")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("accessToken")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("username")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("unban")).map(JsonElement::getAsLong).orElse(0L),
            Optional.ofNullable(jsonObject.get("type")).map(JsonElement::getAsString).map(AccountType::valueOf).orElse(AccountType.PREMIUM) // Default to PREMIUM if type is missing
    );
  }

  @Override
  public String toString() {
    return "Account{" +
            "refreshToken='" + refreshToken + '\'' +
            ", accessToken='" + accessToken + '\'' +
            ", username='" + username + '\'' +
            ", unban=" + unban +
            ", type=" + type +
            '}';
  }
}
