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
    return am.isAccountInList(getUsername());
  }

  @Override
  public boolean complete() {
    Account account = am.getAccountToAdd(getUsername(), getPassword());
    am.getAccounts().add(account);
    return true;
  }
}
