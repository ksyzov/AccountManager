package me.ksyz.accountmanager;

import net.minecraft.client.gui.GuiScreen;

class GuiEdit extends GuiAbstractInput {
  private final int selected;
  private final String username;
  private final String password;

  public GuiEdit(GuiScreen previousScreen, int selected) {
    super(previousScreen, "Edit");

    Account acc = AccountManager.getInstance().getAccounts().get(selected);
    this.username = acc.getEmail();
    this.password = acc.getPassword();
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
    for (Account acc : AccountManager.getInstance().getAccounts()) {
      if (acc.getEmail().equals(getUsername()) && !acc.getEmail().equals(username)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean complete() {
    Account acc = AccountManager.getInstance().getAccountToAdd(getUsername(), getPassword());
    AccountManager.getInstance().getAccounts().set(selected, acc);
    return true;
  }
}
