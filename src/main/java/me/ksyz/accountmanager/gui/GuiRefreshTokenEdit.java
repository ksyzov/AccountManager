package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.auth.Account;
import me.ksyz.accountmanager.utils.Notification;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiRefreshTokenEdit extends GuiScreen {
  private final GuiScreen previousScreen;
  private final Account account;

  private GuiTextField clientIdField;
  private GuiMaskedTextField tokenField;
  private GuiButton saveButton;
  private GuiButton cancelButton;
  private GuiButton visibilityButton;
  private boolean tokenVisible;
  private boolean saved;

  public GuiRefreshTokenEdit(GuiScreen previousScreen, Account account) {
    this.previousScreen = previousScreen;
    this.account = account;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();

    clientIdField = new GuiTextField(
      0, fontRendererObj, width / 2 - 152, height / 2 - 38, 304, 20
    );
    clientIdField.setMaxStringLength(128);
    clientIdField.setText(account.getOauthClientId());

    tokenField = new GuiMaskedTextField(
      1, fontRendererObj, width / 2 - 152, height / 2, 238, 20
    );
    tokenField.setMaxStringLength(8192);
    tokenField.setText(account.getRefreshToken());
    tokenField.setFocused(true);

    buttonList.add(saveButton = new GuiButton(
      1, width / 2 - 150 - 4, height / 2 + 32, 150, 20, "Save"
    ));
    buttonList.add(cancelButton = new GuiButton(
      3, width / 2 + 4, height / 2 + 32, 150, 20, "Cancel"
    ));
    buttonList.add(visibilityButton = new GuiButton(
      4, width / 2 + 90, height / 2, 62, 20, "Show"
    ));
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    if (tokenField != null) {
      tokenField.setText("");
    }
    if (clientIdField != null) {
      clientIdField.setText("");
    }
  }

  @Override
  public void updateScreen() {
    if (tokenField != null) {
      tokenField.updateCursorCounter();
    }
    if (clientIdField != null) {
      clientIdField.updateCursorCounter();
    }
    if (saveButton != null) {
      boolean hasClientId = clientIdField != null
        && !StringUtils.isBlank(clientIdField.getText());
      boolean hasToken = tokenField != null
        && !StringUtils.isBlank(tokenField.getText());
      saveButton.enabled = hasClientId && hasToken;
    }

    if (saved) {
      mc.displayGuiScreen(new GuiAccountManager(
        previousScreen,
        new Notification(
          TextFormatting.translate("&aRefresh-token account updated!&r"),
          5000L
        )
      ));
      saved = false;
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(
      fontRendererObj, "Edit Refresh Token Account",
      width / 2, height / 2 - 72, 0xFFFFFF
    );
    drawString(fontRendererObj, "Client ID", width / 2 - 152, height / 2 - 50, 0xAAAAAA);
    if (clientIdField != null) {
      clientIdField.drawTextBox();
    }
    drawString(fontRendererObj, "Refresh token", width / 2 - 152, height / 2 - 12, 0xAAAAAA);
    if (tokenField != null) {
      if (tokenVisible) {
        tokenField.drawTextBox();
      } else {
        tokenField.drawMaskedTextBox();
      }
    }
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) {
      actionPerformed(cancelButton);
      return;
    }
    if (keyCode == Keyboard.KEY_RETURN && saveButton != null && saveButton.enabled) {
      actionPerformed(saveButton);
      return;
    }
    if (tokenField != null) {
      tokenField.textboxKeyTyped(typedChar, keyCode);
    }
    if (clientIdField != null) {
      clientIdField.textboxKeyTyped(typedChar, keyCode);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (tokenField != null) {
      tokenField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    if (clientIdField != null) {
      clientIdField.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) {
      return;
    }

    switch (button.id) {
      case 1: { // Save
        account.setOauthClientId(clientIdField.getText().trim());
        account.setRefreshToken(tokenField.getText().trim());
        AccountManager.save();
        saved = true;
      }
      break;
      case 3: { // Cancel
        mc.displayGuiScreen(new GuiAccountManager(previousScreen));
      }
      break;
      case 4: { // Show/hide token
        tokenVisible = !tokenVisible;
        visibilityButton.displayString = tokenVisible ? "Hide" : "Show";
        tokenField.setFocused(true);
      }
      break;
    }
  }

}
