package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.Account;
import me.ksyz.accountmanager.AccountManager;
import net.minecraft.client.gui.GuiScreen;

class GuiEdit extends GuiAbstractInput {
  private final String username;
  private final String password;
  private final int selected;

  public GuiEdit(final GuiScreen previousScreen, final int selected) {
    super(previousScreen, "Edit");
    final Account account = AccountManager.getAccounts().get(selected);
    if (account.getPassword().isEmpty()) {
      this.username = account.getUsername();
    } else {
      this.username = account.getEmail();
    }
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
  public void complete() {
    if (getPassword().isEmpty()) {
      AccountManager.getAccounts().set(selected, new Account(getUsername()));
    } else {
      AccountManager.getAccounts().set(selected, new Account(getUsername(), getPassword()));
    }
  }
}
