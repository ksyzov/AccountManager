package me.Vxrtrauter.accountmanager.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiAddAccount extends GuiScreen {
    private final GuiScreen previousScreen;

    public GuiAddAccount(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        // Add buttons for account types
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 - 30, 200, 20, "Microsoft"));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 2, 200, 20, "Cookie"));
        buttonList.add(new GuiButton(2, width / 2 - 100, height / 2 + 30, 200, 20, "Cracked"));
        buttonList.add(new GuiButton(3, width / 2 - 100, height / 2 + 60, 200, 20, "Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw the background
        drawDefaultBackground();

        // Draw the title
        drawCenteredString(fontRendererObj, "Choose Account Type to Add", width / 2, height / 2 - 60, 0xFFFFFF);

        // Draw buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) {
            return;
        }

        switch (button.id) {
            case 0: // Microsoft
                mc.displayGuiScreen(new GuiMicrosoftAuth(this));
                break;
            case 1: // Cookie
                mc.displayGuiScreen(new GuiCookieAuth(this));
                break;
            case 2: // Cracked
                mc.displayGuiScreen(new GuiCrackedAuth(this));
                break;
            case 3: // Back
                mc.displayGuiScreen(new GuiAccountManager(previousScreen));
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        // Cleanup if needed
    }
}