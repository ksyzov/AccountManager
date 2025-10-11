package me.lquifi.accountmanager.gui;

import me.lquifi.accountmanager.auth.TokenAuth;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GuiTokenAuth extends GuiScreen {
    private final GuiScreen previousScreen;
    private GuiTextField tokenField;
    private GuiButton loginButton;
    private GuiButton cancelButton;
    private String status = null;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public GuiTokenAuth(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        tokenField = new GuiTextField(0, this.fontRendererObj, width / 2 - 100, height / 2 - 10, 200, 20);
        tokenField.setMaxStringLength(1000);
        tokenField.setFocused(true);

        buttonList.clear();
        buttonList.add(loginButton = new GuiButton(0, width / 2 - 75 - 2, height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT, 75, 20, "Login"));
        buttonList.add(cancelButton = new GuiButton(1, width / 2 + 2, height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT, 75, 20, "Cancel"));

        status = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(
                fontRendererObj, "Token Login",
                width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810
        );
        tokenField.drawTextBox();

        if (status != null) {
            drawCenteredString(fontRendererObj, status, width / 2, height / 2 + 40, -1);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        tokenField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            handleLogin();
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            actionPerformed(cancelButton);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        tokenField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || !button.enabled) {
            return;
        }

        switch (button.id) {
            case 0:
                handleLogin();
                break;
            case 1:
                mc.displayGuiScreen(previousScreen);
                break;
        }
    }

    private void handleLogin() {
        String token = tokenField.getText().trim();

        if (token.isEmpty()) {
            status = "§cEnter a token dumbass.";
            return;
        }

        status = "Logging in...";

        CompletableFuture.runAsync(() -> {
            boolean loginSuccess = TokenAuth.login(token);

            SwingUtilities.invokeLater(() -> {
                if (loginSuccess) {
                    status = "§aSuccessfully logged in!";

                    scheduler.schedule(() -> {
                        mc.displayGuiScreen(new GuiAccountManager(previousScreen));
                    }, 500, TimeUnit.MILLISECONDS);

                } else {
                    status = "§cFailed to log in.";
                }
            });
        });
    }


    @Override
    public void updateScreen() {
        tokenField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
