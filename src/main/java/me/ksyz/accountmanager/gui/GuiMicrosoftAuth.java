package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.auth.MicrosoftAuth;
import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.utils.Notification;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiMicrosoftAuth extends GuiScreen {
  private final GuiScreen previousScreen;

  private GuiButton cancelButton = null;
  private String status = null;
  private String cause = null;
  private ExecutorService executor = null;
  private CompletableFuture<Void> task = null;

  public GuiMicrosoftAuth(final GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    buttonList.clear();

    buttonList.add(cancelButton = new GuiButton(
      0, width / 2 - 100, height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT, "Cancel"
    ));
    if (task == null) {
      if (executor == null) {
        executor = Executors.newSingleThreadExecutor();
      }
      status = "&rCheck your browser to continue...&r";
      task = MicrosoftAuth.acquireMSAuthCode(executor)
        .thenComposeAsync(msAuthCode -> {
          status = "&rAcquiring Microsoft access token&r";
          return MicrosoftAuth.acquireMSAccessToken(msAuthCode, executor);
        })
        .thenComposeAsync(msAccessToken -> {
          status = "&rAcquiring Xbox access token&r";
          return MicrosoftAuth.acquireXboxAccessToken(msAccessToken, executor);
        })
        .thenComposeAsync(xboxAccessToken -> {
          status = "&rAcquiring Xbox XSTS token&r";
          return MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor);
        })
        .thenComposeAsync(xboxXstsData -> {
          status = "&rAcquiring Minecraft access token&r";
          return MicrosoftAuth.acquireMCAccessToken(
            xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor
          );
        })
        .thenComposeAsync(mcToken -> {
          status = "&rFetching your Minecraft profile&r";
          return MicrosoftAuth.login(mcToken, executor);
        })
        .thenAccept(session -> {
          SessionManager.setSession(session);
          status = null;
          Notification.setNotification(
            String.format("Successful login! (%s)", session.getUsername()),
            TextFormatting.GREEN.getRGB()
          );
          actionPerformed(cancelButton);
        })
        .exceptionally(error -> {
          status = String.format("&c%s&r", error.getMessage());
          cause = String.format("&c&o%s&r", error.getCause().getMessage());
          return null;
        });
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
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);

    drawCenteredString(
      fontRendererObj, "Microsoft Authentication",
      width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810
    );
    if (status != null) {
      drawCenteredString(
        fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2, -1
      );
    }
    if (cause != null) {
      final String causeText = TextFormatting.translate(cause);
      Gui.drawRect(
        0, height - 2 - fontRendererObj.FONT_HEIGHT - 2,
        2 + mc.fontRendererObj.getStringWidth(causeText) + 2, height,
        0x64000000
      );
      Gui.drawRect(
        0, height - 1,
        2 + mc.fontRendererObj.getStringWidth(causeText) + 2, height,
        0xFF000000
      );
      drawString(
        fontRendererObj, TextFormatting.translate(cause),
        2, height - 2 - fontRendererObj.FONT_HEIGHT, -1
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
    if (button != null && button.id == 0) {
      mc.displayGuiScreen(new GuiAccountManager(previousScreen, false));
    }
  }
}
