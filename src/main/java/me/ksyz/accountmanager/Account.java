package me.ksyz.accountmanager;

public class Account {
  private String email, password, uuid, name, type;

  public Account(String email, String password, String uuid, String name, String type) {
    this.email = email;
    this.password = password;
    this.uuid = uuid;
    this.name = name;
    this.type = type;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }
}
