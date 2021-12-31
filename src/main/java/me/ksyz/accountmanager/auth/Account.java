package me.ksyz.accountmanager.auth;

public class Account {
  private final String email;
  private final String password;
  private String userType;
  private String username;

  public Account(String email, String password, String userType, String username) {
    this.email = email;
    this.password = password;
    this.userType = userType;
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getUserType() {
    return userType;
  }

  public String getUsername() {
    return username;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
