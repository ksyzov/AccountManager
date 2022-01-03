package me.ksyz.accountmanager.account;

public abstract class Account {
  private String username;

  protected Account(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
