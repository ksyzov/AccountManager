package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.Account;
import me.ksyz.accountmanager.AccountManager;
import net.minecraft.client.gui.GuiScreen;

class GuiEdit extends GuiAbstractInput {
  private final int selected;

  public GuiEdit(final GuiScreen previousScreen, final int selected) {
    super(previousScreen, "Edit");
    this.selected = selected;
  }

  @Override
  public void initGui() {
    super.initGui();
    final Account account = AccountManager.getAccounts().get(selected);
    if (account.getPassword().isEmpty()) {
      setUsername(account.getUsername());
    } else {
      setUsername(account.getEmail());
    }
    setPassword(account.getPassword());
  }

  @Override
  public void complete() {
    if (getPassword().isEmpty()) {
      AccountManager.getAccounts().set(
        selected, new Account(getUsername())
      );
    } else {
      final Account account = AccountManager.getAccounts().get(selected);
      AccountManager.getAccounts().set(
        selected, new Account(getUsername(), getPassword(), account.getUsername())
      );
    }
  }
}
