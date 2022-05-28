package me.ksyz.accountmanager;

public class Account {
  private final String email;
  private final String password;
  private String username;

  public Account(final String email, final String password, final String username) {
    this.email = email;
    this.password = password;
    this.username = username;
  }

  public Account(final String email, final String password) {
    this.email = email;
    this.password = password;
    this.username = "";
  }

  public Account(final String username) {
    this.email = "";
    this.password = "";
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }
}
