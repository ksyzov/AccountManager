package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.account.Account;
import me.ksyz.accountmanager.account.LegacyAccount;
import me.ksyz.accountmanager.account.MojangAccount;
import net.minecraft.client.gui.GuiScreen;

class GuiEdit extends GuiAbstractInput {
  private static final AccountManager am = AccountManager.getAccountManager();

  private final int selected;
  private final String username;
  private final String password;

  public GuiEdit(GuiScreen previousScreen, int selected) {
    super(previousScreen, "Edit");
    this.selected = selected;
    Account account = am.getAccounts().get(selected);
    if (account instanceof LegacyAccount) {
      this.username = account.getUsername();
      this.password = "";
    } else if (account instanceof MojangAccount) {
      this.username = ((MojangAccount) account).getEmail();
      this.password = ((MojangAccount) account).getPassword();
    } else {
      this.username = "";
      this.password = "";
    }
  }

  @Override
  public void initGui() {
    super.initGui();
    setUsername(username);
    setPassword(password);
  }

  @Override
  public boolean isAccountInList() {
    for (Account acc : am.getAccounts()) {
      if (
        acc instanceof LegacyAccount &&
        acc.getUsername().equals(getUsername()) &&
        !acc.getUsername().equals(username)
      ) {
        return true;
      }
      if (
        acc instanceof MojangAccount &&
        ((MojangAccount) acc).getEmail().equals(getUsername()) &&
        !((MojangAccount) acc).getEmail().equals(username)
      ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean complete() {
    if (getPassword().equals("")) {
      am.getAccounts().set(selected, new LegacyAccount(getUsername()));
    } else {
      am.getAccounts().set(selected, new MojangAccount(getUsername(), getPassword()));
    }
    return true;
  }
}
