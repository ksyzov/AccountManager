package me.Vxrtrauter.accountmanager.gui;

import me.Vxrtrauter.accountmanager.auth.CookieAuth;
import me.Vxrtrauter.accountmanager.utils.Notification;
import me.Vxrtrauter.accountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class GuiCookieAuth extends GuiScreen {
    private final GuiScreen previousScreen;
    private ExecutorService executor = null;
    private CompletableFuture<Void> task = null;

    private GuiButton openButton = null;
    private boolean openButtonEnabled = true;
    private GuiButton cancelButton = null;
    public String status = null;
    private String cause = null;

    public GuiCookieAuth(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(openButton = new GuiButton(
                0,
                width / 2 - 75 - 2,
                height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT,
                75,
                20,
                "Open"
        ));
        buttonList.add(cancelButton = new GuiButton(
                1,
                width / 2 + 2,
                height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT,
                75,
                20,
                "Cancel"
        ));

        status = "&fSelect a cookie file to authenticate&r";

        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void onGuiClosed() {
        if (task != null && !task.isDone()) {
            task.cancel(true);
            executor.shutdownNow();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (openButton != null) {
            openButton.enabled = openButtonEnabled;
        }
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(
                fontRendererObj, "Cookie Authentication",
                width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810
        );

        if (status != null) {
            drawCenteredString(
                    fontRendererObj, TextFormatting.translate(status),
                    width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2, -1
            );
        }

        if (cause != null) {
            String causeText = TextFormatting.translate(cause);
            Gui.drawRect(
                    0, height - 2 - fontRendererObj.FONT_HEIGHT - 3,
                    3 + mc.fontRendererObj.getStringWidth(causeText) + 3, height,
                    0x64000000
            );
            drawString(
                    fontRendererObj, TextFormatting.translate(cause),
                    3, height - 2 - fontRendererObj.FONT_HEIGHT, -1
            );
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            actionPerformed(cancelButton);
        }
    }




    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) {
            return;
        }

        if (button.enabled) {
            switch (button.id) {
                case 0: { // Open button
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SwingUtilities.invokeLater(() -> {
                        FileDialog fileDialog = new FileDialog((Frame) null, "Select Cookie File", FileDialog.LOAD);
                        fileDialog.setDirectory(System.getProperty("user.home") + File.separator + "Downloads");
                        status = "&aFile Picker has been opened in the Background!&r";
                        fileDialog.setFile("*.txt");
                        fileDialog.setModal(true);
                        fileDialog.setVisible(true);
                        String selectedFileName = fileDialog.getFile();
                        if (selectedFileName != null) {
                            File selectedFile = new File(fileDialog.getDirectory(), selectedFileName);
                            if (selectedFile.exists()) {
                                openButtonEnabled = false;
                                status = "&fReading cookie file...&r";
                                CompletableFuture<Boolean> authTask = CookieAuth.addAccountFromCookieFile(selectedFile, this);
                                authTask.thenAccept(success -> {
                                    if (success) {
                                        mc.displayGuiScreen(new GuiAccountManager(previousScreen));
                                    }
                                });
                            } else {
                                status = "&cSelected file does not exist!&r";
                            }
                        } else {
                            status = "&eFile selection canceled.&r";
                        }
                    });
                    break;
                }
                case 1: { // Cancel button
                    mc.displayGuiScreen(previousScreen);
                    break;
                }
                default: {
                    System.err.println("Unknown button ID: " + button.id);
                    break;
                }
            }
        }
    }
}