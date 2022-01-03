package me.ksyz.accountmanager.account;

public class MojangAccount extends Account {
  private final String email;
  private final String password;

  public MojangAccount(String email, String password, String username) {
    super(username);
    this.email = email;
    this.password = password;
  }

  public MojangAccount(String email, String password) {
    super("");
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }
}
