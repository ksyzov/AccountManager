package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.auth.MicrosoftAuth;
import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiMicrosoftAuth extends GuiScreen {
  private static final AccountManager am = AccountManager.getAccountManager();

  private final GuiScreen previousScreen;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private CompletableFuture<Void> task = null;
  private String status = null;

  public GuiMicrosoftAuth(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    buttonList.clear();
    buttonList.add(new GuiButton(
      0, width / 2 - 100, height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT, "Cancel"
    ));

    if (task != null) {
      return;
    }
    status = "&oCheck your browser to continue...&r";
    task = MicrosoftAuth
      .acquireMSAuthCode(success -> "Close this window and return to Minecraft!", executor)
      .thenComposeAsync(msAuthCode -> {
        status = "&oAcquiring Microsoft access token&r";
        return MicrosoftAuth.acquireMSAccessToken(msAuthCode, executor);
      })
      .thenComposeAsync(msAccessToken -> {
        status = "&oAcquiring Xbox access token&r";
        return MicrosoftAuth.acquireXboxAccessToken(msAccessToken, executor);
      })
      .thenComposeAsync(xboxAccessToken -> {
        status = "&oAcquiring Xbox XSTS token&r";
        return MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor);
      })
      .thenComposeAsync(xboxXstsData -> {
        status = "&oAcquiring Minecraft access token&r";
        return MicrosoftAuth.acquireMCAccessToken(
          xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor
        );
      })
      .thenComposeAsync(mcToken -> {
        status = "&oFetching your Minecraft profile&r";
        return MicrosoftAuth.login(mcToken, executor);
      })
      .thenAccept(session -> {
        mc.displayGuiScreen(previousScreen);
        SessionManager.setSession(session);
      })
      .exceptionally(error -> {
        status = String.format("&c%s&r", error.getMessage());
        return null;
      });
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
    this.drawDefaultBackground();
    if (status != null) {
      this.drawCenteredString(
        fontRendererObj, TextFormatting.translateAlternateColorCodes(status),
        width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810
      );
    }
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id == 0) {
      mc.displayGuiScreen(previousScreen);
    }
  }
}
