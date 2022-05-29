package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public abstract class GuiAbstractInput extends GuiScreen {
  private final GuiScreen previousScreen;
  private final String actionMessage;

  private GuiTextField usernameField = null;
  private GuiTextField passwordField = null;
  private GuiButton completeButton = null;
  private GuiButton cancelButton = null;

  public GuiAbstractInput(final GuiScreen previousScreen, final String actionMessage) {
    this.previousScreen = previousScreen;
    this.actionMessage = actionMessage;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();

    // Buttons
    buttonList.add(completeButton = new GuiButton(
      2, width / 2 - 152, height / 2 + 15, 150, 20, actionMessage
    ));
    completeButton.enabled = false;
    buttonList.add(cancelButton = new GuiButton(
      3, width / 2 + 2, height / 2 + 15, 150, 20, "Cancel"
    ));

    // Fields
    usernameField = new GuiTextField(
      0, fontRendererObj, width / 2 - 100, height / 2 - 45, 200, 20
    );
    usernameField.setFocused(true);
    usernameField.setMaxStringLength(64);
    passwordField = new GuiTextField(
      1, fontRendererObj, width / 2 - 100, height / 2 - 15, 200, 20
    );
    passwordField.setMaxStringLength(64);
  }

  @Override
  public void onGuiClosed() {
    AccountManager.save();
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void updateScreen() {
    completeButton.enabled = !StringUtils.isBlank(getUsername());
    usernameField.updateCursorCounter();
    passwordField.updateCursorCounter();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
    drawDefaultBackground();
    usernameField.drawTextBox();
    passwordField.drawTextBox();
    super.drawScreen(mouseX, mouseY, renderPartialTicks);

    drawCenteredString(
      fontRendererObj, actionMessage, width / 2, 7, -1
    );
    drawCenteredString(
      fontRendererObj, "Username", width / 2 - 130, height / 2 - 39, 0xFFA0A0A0
    );
    drawCenteredString(
      fontRendererObj, "Password", width / 2 - 130, height / 2 - 9, 0xFFA0A0A0
    );
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    usernameField.mouseClicked(mouseX, mouseY, mouseButton);
    passwordField.mouseClicked(mouseX, mouseY, mouseButton);
    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    switch (keyCode) {
      case Keyboard.KEY_ESCAPE: {
        actionPerformed(cancelButton);
      }
      break;
      case Keyboard.KEY_RETURN: {
        actionPerformed(completeButton);
      }
      break;
      case Keyboard.KEY_TAB: {
        if (usernameField.isFocused() || passwordField.isFocused()) {
          usernameField.setFocused(!usernameField.isFocused());
          passwordField.setFocused(!passwordField.isFocused());
        }
      }
      break;
      default: {
        usernameField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);
      }
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.enabled) {
      switch (button.id) {
        case 2: {
          complete();
          actionPerformed(cancelButton);
        }
        break;
        case 3: {
          mc.displayGuiScreen(new GuiAccountManager(previousScreen, false));
        }
        break;
      }
    }
  }

  public abstract void complete();

  public String getUsername() {
    return usernameField.getText();
  }

  public String getPassword() {
    return passwordField.getText();
  }

  public void setUsername(final String username) {
    usernameField.setText(username);
  }

  public void setPassword(final String password) {
    passwordField.setText(password);
  }
}
