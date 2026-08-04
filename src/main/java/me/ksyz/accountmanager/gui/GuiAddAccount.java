package me.ksyz.accountmanager.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class GuiAddAccount extends GuiScreen {
  private final GuiScreen previousScreen;

  public GuiAddAccount(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    buttonList.clear();
    buttonList.add(new GuiButton(
      0, width / 2 - 100, height / 2 - 24, 200, 20, "Microsoft Login"
    ));
    buttonList.add(new GuiButton(
      1, width / 2 - 100, height / 2, 200, 20, "Refresh Token"
    ));
    buttonList.add(new GuiButton(
      2, width / 2 - 100, height / 2 + 24, 200, 20, "Offline"
    ));
    buttonList.add(new GuiButton(
      3, width / 2 - 100, height / 2 + 60, 200, 20, "Cancel"
    ));
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(
      fontRendererObj, "Choose an authentication method",
      width / 2, height / 2 - 56, 0xFFFFFF
    );
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) {
      mc.displayGuiScreen(new GuiAccountManager(previousScreen));
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) {
      return;
    }

    switch (button.id) {
      case 0:
        mc.displayGuiScreen(new GuiMicrosoftAuth(previousScreen));
        break;
      case 1:
        mc.displayGuiScreen(new GuiRefreshTokenAuth(previousScreen));
        break;
      case 2:
        mc.displayGuiScreen(new GuiOfflineAuth(previousScreen));
        break;
      case 3:
        mc.displayGuiScreen(new GuiAccountManager(previousScreen));
        break;
    }
  }
}
