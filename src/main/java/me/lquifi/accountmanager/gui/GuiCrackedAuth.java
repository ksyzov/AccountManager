package me.lquifi.accountmanager.gui;

import me.lquifi.accountmanager.auth.CrackedAuth;
import me.lquifi.accountmanager.utils.UsernameGenerator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GuiCrackedAuth extends GuiScreen {
    private final GuiScreen previousScreen;
    private GuiTextField usernameField;
    private GuiButton loginButton;
    private GuiButton generateRandomButton;

    public GuiCrackedAuth(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {

        usernameField = new GuiTextField(0, this.fontRendererObj, width / 2 - 100, height / 2 - 30, 200, 20);
        usernameField.setMaxStringLength(16); // Minecraft usernames are limited to 16 characters
        usernameField.setFocused(true);
        buttonList.add(loginButton = new GuiButton(0, width / 2 - 100, height / 2, 200, 20, "Login"));
        buttonList.add(generateRandomButton = new GuiButton(1, width / 2 - 100, height / 2 + 30, 200, 20, "Generate Random"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Cracked Authentication", width / 2, height / 2 - 60, 0xFFFFFF);
        usernameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        usernameField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(previousScreen);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || !button.enabled) {
            return;
        }

        switch (button.id) {
            case 0: // Login
                handleLogin();
                break;
            case 1: // Generate Random
                handleGenerateRandom();
                break;
            default:
                System.err.println("Unknown button ID: " + button.id);
                break;
        }
    }

    private void handleLogin() {

        String username = usernameField.getText().trim();


        if (username.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        boolean loginSuccess = CrackedAuth.login(username);

        if (loginSuccess) {
            System.out.println("Logged in successfully as: " + username);
            mc.displayGuiScreen(new GuiAccountManager(previousScreen));

        } else {
            System.out.println("Failed to log in!");
        }
    }

    private void handleGenerateRandom() {
        CompletableFuture.runAsync(() -> {
            String randomUsername = UsernameGenerator.generate();
            SwingUtilities.invokeLater(() -> usernameField.setText(randomUsername));
        });
    }


    @Override
    public void updateScreen() {
        usernameField.updateCursorCounter();
    }
}