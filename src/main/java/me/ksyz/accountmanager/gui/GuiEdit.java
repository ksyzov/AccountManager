package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.auth.Account;
import net.minecraft.client.gui.GuiScreen;

class GuiEdit extends GuiAbstractInput {
  private static final AccountManager am = AccountManager.getAccountManager();

  private final int selected;
  private final String username;
  private final String password;

  public GuiEdit(GuiScreen previousScreen, int selected) {
    super(previousScreen, "Edit");
    Account account = am.getAccounts().get(selected);
    this.username = account.getEmail();
    this.password = account.getPassword();
    this.selected = selected;
  }

  @Override
  public void initGui() {
    super.initGui();
    setUsername(username);
    setPassword(password);
  }

  @Override
  public boolean isAccountInList() {
    for (Account account : am.getAccounts()) {
      if (account.getEmail().equals(getUsername()) && !account.getEmail().equals(username)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean complete() {
    Account account = am.getAccountToAdd(getUsername(), getPassword());
    am.getAccounts().set(selected, account);
    return true;
  }
}
