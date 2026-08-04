package me.ksyz.accountmanager.gui;

import me.ksyz.accountmanager.AccountManager;
import me.ksyz.accountmanager.auth.Account;
import me.ksyz.accountmanager.auth.MicrosoftAuth;
import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.utils.Notification;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GuiRefreshTokenAuth extends GuiScreen {
  private final GuiScreen previousScreen;

  private GuiTextField clientIdField;
  private GuiMaskedTextField tokenField;
  private GuiButton addButton;
  private GuiButton visibilityButton;
  private String status;
  private ExecutorService executor;
  private CompletableFuture<Void> task;
  private boolean success;
  private boolean tokenVisible;

  public GuiRefreshTokenAuth(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();

    clientIdField = new GuiTextField(
      0, fontRendererObj, width / 2 - 152, height / 2 - 38, 304, 20
    );
    clientIdField.setMaxStringLength(128);
    clientIdField.setText(MicrosoftAuth.DEFAULT_REFRESH_TOKEN_CLIENT_ID);

    tokenField = new GuiMaskedTextField(
      1, fontRendererObj, width / 2 - 152, height / 2, 238, 20
    );
    tokenField.setMaxStringLength(8192);
    tokenField.setFocused(true);

    buttonList.add(addButton = new GuiButton(
      1, width / 2 - 75 - 2, height / 2 + 32, 75, 20, "Add"
    ));
    buttonList.add(new GuiButton(
      2, width / 2 + 2, height / 2 + 32, 75, 20, "Cancel"
    ));
    buttonList.add(visibilityButton = new GuiButton(
      3, width / 2 + 90, height / 2, 62, 20, "Show"
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
    if (task != null && !task.isDone()) {
      task.cancel(true);
      executor.shutdownNow();
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
    if (addButton != null) {
      addButton.enabled = task == null
        && tokenField != null
        && clientIdField != null
        && !StringUtils.isBlank(tokenField.getText())
        && !StringUtils.isBlank(clientIdField.getText());
    }

    if (success) {
      mc.displayGuiScreen(new GuiAccountManager(
        previousScreen,
        new Notification(
          TextFormatting.translate(String.format(
            "&aRefresh-token account added! (%s)&r", SessionManager.get().getUsername()
          )),
          5000L
        )
      ));
      success = false;
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(
      fontRendererObj, "Add with Refresh Token",
      width / 2, height / 2 - 72, 0xFFFFFF
    );
    drawString(fontRendererObj, "Client ID", width / 2 - 152, height / 2 - 50, 0xAAAAAA);
    if (clientIdField != null) {
      clientIdField.drawTextBox();
    }
    drawString(
      fontRendererObj, "Refresh token",
      width / 2 - 152, height / 2 - 12, 0xAAAAAA
    );
    if (tokenField != null) {
      if (tokenVisible) {
        tokenField.drawTextBox();
      } else {
        tokenField.drawMaskedTextBox();
      }
    }
    if (status != null) {
      drawCenteredString(
        fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 + 62, -1
      );
    }
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) {
      mc.displayGuiScreen(new GuiAddAccount(previousScreen));
      return;
    }
    if (keyCode == Keyboard.KEY_RETURN && addButton != null && addButton.enabled) {
      actionPerformed(addButton);
      return;
    }
    if (tokenField != null && task == null) {
      tokenField.textboxKeyTyped(typedChar, keyCode);
    }
    if (clientIdField != null && task == null) {
      clientIdField.textboxKeyTyped(typedChar, keyCode);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (tokenField != null && task == null) {
      tokenField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    if (clientIdField != null && task == null) {
      clientIdField.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) {
      return;
    }

    if (button.id == 2) {
      mc.displayGuiScreen(new GuiAddAccount(previousScreen));
      return;
    }
    if (button.id == 3) {
      tokenVisible = !tokenVisible;
      visibilityButton.displayString = tokenVisible ? "Hide" : "Show";
      tokenField.setFocused(true);
      return;
    }
    if (button.id != 1 || task != null) {
      return;
    }

    final String oauthClientId = clientIdField.getText().trim();
    final String suppliedRefreshToken = tokenField.getText().trim();
    tokenField.setText("");
    tokenField.setEnabled(false);
    clientIdField.setEnabled(false);
    executor = Executors.newSingleThreadExecutor();

    AtomicReference<String> rotatedRefreshToken = new AtomicReference<>(suppliedRefreshToken);
    AtomicReference<String> minecraftAccessToken = new AtomicReference<>("");
    status = "&7Validating refresh token...&r";

    task = MicrosoftAuth.refreshMSAccessTokens(
        suppliedRefreshToken, Account.AuthenticationType.REFRESH_TOKEN, oauthClientId, executor
      )
      .thenComposeAsync(msAccessTokens -> {
        rotatedRefreshToken.set(msAccessTokens.get("refresh_token"));
        status = "&7Acquiring Xbox access token...&r";
        return MicrosoftAuth.acquireXboxAccessToken(
          msAccessTokens.get("access_token"),
          Account.AuthenticationType.REFRESH_TOKEN,
          oauthClientId,
          executor
        );
      }, executor)
      .thenComposeAsync(xboxAccessToken -> {
        status = "&7Acquiring Xbox XSTS token...&r";
        return MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor);
      }, executor)
      .thenComposeAsync(xboxXstsData -> {
        status = "&7Acquiring Minecraft access token...&r";
        return MicrosoftAuth.acquireMCAccessToken(
          xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor
        );
      }, executor)
      .thenComposeAsync(mcToken -> {
        minecraftAccessToken.set(mcToken);
        status = "&7Fetching your Minecraft profile...&r";
        return MicrosoftAuth.login(mcToken, executor);
      }, executor)
      .thenAccept(session -> finishAddingAccount(
        session, rotatedRefreshToken.get(), minecraftAccessToken.get(), oauthClientId
      ))
      .exceptionally(error -> {
        status = String.format("&c%s&r", getErrorMessage(error));
        executor.shutdown();
        task = null;
        if (tokenField != null) {
          tokenField.setText(rotatedRefreshToken.get());
          tokenField.setEnabled(true);
          tokenField.setFocused(true);
        }
        if (clientIdField != null) {
          clientIdField.setEnabled(true);
        }
        return null;
      });
  }

  private void finishAddingAccount(
    Session session,
    String refreshToken,
    String accessToken,
    String oauthClientId
  ) {
    Account account = new Account(
      refreshToken,
      accessToken,
      session.getUsername(),
      0L,
      Account.AuthenticationType.REFRESH_TOKEN,
      oauthClientId,
      MicrosoftAuth.getAccessTokenExpiration(accessToken),
      ""
    );
    for (Account existing : AccountManager.accounts) {
      if (account.getUsername().equals(existing.getUsername())) {
        account.setUnban(existing.getUnban());
        break;
      }
    }
    AccountManager.accounts.add(account);
    AccountManager.save();
    SessionManager.set(session);
    status = null;
    success = true;
    executor.shutdown();
  }

  private static String getErrorMessage(Throwable error) {
    Throwable current = error;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return StringUtils.defaultIfBlank(current.getMessage(), "Authentication failed.");
  }
}
