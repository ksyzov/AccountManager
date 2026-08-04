package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.auth.Account;
import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.utils.Notification;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiOfflineAuth extends GuiScreen {
  private final GuiScreen previousScreen;

  private GuiTextField usernameField;
  private GuiButton addButton;
  private GuiButton cancelButton;
  private String status;
  private boolean success;

  public GuiOfflineAuth(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();

    usernameField = new GuiTextField(
      0, fontRendererObj, width / 2 - 152, height / 2 - 12, 304, 20
    );
    usernameField.setMaxStringLength(16);
    usernameField.setFocused(true);

    buttonList.add(addButton = new GuiButton(
      1, width / 2 - 75 - 2, height / 2 + 20, 75, 20, "Add"
    ));
    buttonList.add(cancelButton = new GuiButton(
      2, width / 2 + 2, height / 2 + 20, 75, 20, "Cancel"
    ));
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    if (usernameField != null) {
      usernameField.setText("");
    }
  }

  @Override
  public void updateScreen() {
    if (usernameField != null) {
      usernameField.updateCursorCounter();
      addButton.enabled = !StringUtils.isBlank(usernameField.getText());
    }

    if (success) {
      mc.displayGuiScreen(new GuiAccountManager(
        previousScreen,
        new Notification(
          TextFormatting.translate(String.format(
            "&aOffline account added! (%s)&r", SessionManager.get().getUsername()
          )),
          5000L
        )
      ));
      success = false;
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(
      fontRendererObj, "Add Offline Account",
      width / 2, height / 2 - 48, 0xFFFFFF
    );
    drawString(fontRendererObj, "Username", width / 2 - 152, height / 2 - 26, 0xAAAAAA);
    if (usernameField != null) {
      usernameField.drawTextBox();
    }
    if (status != null) {
      drawCenteredString(
        fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 + 52, -1
      );
    }
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) {
      actionPerformed(cancelButton);
      return;
    }
    if (keyCode == Keyboard.KEY_RETURN && addButton != null && addButton.enabled) {
      actionPerformed(addButton);
      return;
    }
    if (usernameField != null) {
      usernameField.textboxKeyTyped(typedChar, keyCode);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (usernameField != null) {
      usernameField.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) {
      return;
    }

    if (button.id == 2) {
      mc.displayGuiScreen(new GuiAddAccount(previousScreen));
      return;
    }
    if (button.id != 1) {
      return;
    }

    String username = usernameField.getText().trim();
    Account account = new Account(
      "",
      "",
      username,
      0L,
      Account.AuthenticationType.OFFLINE,
      "",
      0L,
      ""
    );
    if (AccountManager.findDuplicateAccount(account) != null) {
      status = "&cAn offline account with this username already exists.&r";
      return;
    }

    for (Account existing : AccountManager.accounts) {
      if (username.equalsIgnoreCase(existing.getUsername())) {
        account.setUnban(existing.getUnban());
        break;
      }
    }
    AccountManager.accounts.add(account);
    AccountManager.save();
    SessionManager.set(new Session(
      username, "", "", Session.Type.LEGACY.toString()
    ));
    success = true;
  }
}
