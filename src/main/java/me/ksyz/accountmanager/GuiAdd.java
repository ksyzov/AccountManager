package me.ksyz.accountmanager;

import net.minecraft.client.gui.GuiScreen;

public class GuiAdd extends GuiAbstractInput {
  public GuiAdd(GuiScreen previousScreen) {
    super(previousScreen, "Add");
  }

  @Override
  public boolean isAccountInList() {
    for (Account acc : AccountManager.getInstance().getAccounts()) {
      if (acc.getEmail().equals(getUsername())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean complete() {
    Account acc = AccountManager.getInstance().getAccountToAdd(getUsername(), getPassword());
    AccountManager.getInstance().getAccounts().add(acc);
    return true;
  }
}
