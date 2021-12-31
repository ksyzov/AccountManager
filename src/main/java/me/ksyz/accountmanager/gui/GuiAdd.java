package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.auth.Account;
import net.minecraft.client.gui.GuiScreen;

public class GuiAdd extends GuiAbstractInput {
  private static final AccountManager am = AccountManager.getAccountManager();

  public GuiAdd(GuiScreen previousScreen) {
    super(previousScreen, "Add");
  }

  @Override
  public boolean isAccountInList() {
    for (Account acc : am.getAccounts()) {
      if (acc.getEmail().equals(getUsername())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean complete() {
    Account acc = am.getAccountToAdd(getUsername(), getPassword());
    am.getAccounts().add(acc);
    return true;
  }
}
