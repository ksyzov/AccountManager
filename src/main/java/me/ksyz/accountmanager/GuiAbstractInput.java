package me.ksyz.accountmanager;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public abstract class GuiAbstractInput extends GuiScreen {
  private final GuiScreen previousScreen;
  private final String actionString;
  private GuiTextField usernameField;
  private GuiTextField passwordField;
  private GuiButton complete;

  public GuiAbstractInput(GuiScreen previousScreen, String actionString) {
    this.previousScreen = previousScreen;
    this.actionString = actionString;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    this.buttonList.clear();
    this.buttonList
        .add(complete = new GuiButton(2, this.width / 2 - 152, this.height / 2 + 15, 150, 20, this.actionString));
    this.buttonList.add(new GuiButton(3, this.width / 2 + 2, this.height / 2 + 15, 150, 20, "Cancel"));
    usernameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 45, 200, 20);
    usernameField.setFocused(true);
    usernameField.setMaxStringLength(64);
    passwordField = new GuiTextField(1, this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 15, 200, 20);
    passwordField.setMaxStringLength(64);
    complete.enabled = false;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
    this.drawDefaultBackground();
    this.drawCenteredString(fontRendererObj, this.actionString, this.width / 2, 7, -1);
    this.drawCenteredString(fontRendererObj, "Username", this.width / 2 - 130, this.height / 2 - 39, 0xFFA0A0A0);
    usernameField.drawTextBox();
    this.drawCenteredString(fontRendererObj, "Password", this.width / 2 - 130, this.height / 2 - 9, 0xFFA0A0A0);
    passwordField.drawTextBox();
    super.drawScreen(mouseX, mouseY, renderPartialTicks);
  }

  @Override
  protected void keyTyped(char character, int keyIndex) {
    if (keyIndex == Keyboard.KEY_ESCAPE) {
      escape();
    } else if (keyIndex == Keyboard.KEY_RETURN) {
      if (usernameField.isFocused()) {
        usernameField.setFocused(false);
        passwordField.setFocused(true);
      } else if (passwordField.isFocused() && complete.enabled) {
        complete();
        escape();
      }
    } else if (keyIndex == Keyboard.KEY_TAB && (usernameField.isFocused() || passwordField.isFocused())) {
      usernameField.setFocused(!usernameField.isFocused());
      passwordField.setFocused(!passwordField.isFocused());
    } else {
      // GuiTextField checks if it's focused before doing anything
      usernameField.textboxKeyTyped(character, keyIndex);
      passwordField.textboxKeyTyped(character, keyIndex);
    }
  }

  @Override
  public void updateScreen() {
    usernameField.updateCursorCounter();
    passwordField.updateCursorCounter();
    complete.enabled = canComplete();
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.enabled) {
      if (button.id == 2) {
        if (complete()) {
          escape();
        }
      } else if (button.id == 3) {
        escape();
      }
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    usernameField.mouseClicked(mouseX, mouseY, mouseButton);
    passwordField.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  // Return to the Account Manager GUI
  private void escape() {
    mc.displayGuiScreen(new GuiAccountManager(previousScreen));
  }

  public String getUsername() {
    return usernameField.getText();
  }

  public String getPassword() {
    return passwordField.getText();
  }

  public void setUsername(String username) {
    this.usernameField.setText(username);
  }

  public void setPassword(String password) {
    this.passwordField.setText(password);
  }

  public boolean canComplete() {
    return getUsername().length() > 0 && !isAccountInList();
  }

  public abstract boolean isAccountInList();

  public abstract boolean complete();
}
