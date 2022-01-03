package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.account.LegacyAccount;
import me.ksyz.accountmanager.account.MojangAccount;
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
    if (getPassword().equals("")) {
      am.getAccounts().add(new LegacyAccount(getUsername()));
    } else {
      am.getAccounts().add(new MojangAccount(getUsername(), getPassword()));
    }
    return true;
  }
}
