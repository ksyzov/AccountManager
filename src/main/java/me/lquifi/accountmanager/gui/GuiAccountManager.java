package me.lquifi.accountmanager.gui;

import me.lquifi.accountmanager.AccountManager;
import me.lquifi.accountmanager.auth.Account;
import me.lquifi.accountmanager.auth.AccountType;
import me.lquifi.accountmanager.auth.CrackedAuth;
import me.lquifi.accountmanager.auth.MicrosoftAuth;
import me.lquifi.accountmanager.auth.SessionManager;
import me.lquifi.accountmanager.utils.Notification;
import me.lquifi.accountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiAccountManager extends GuiScreen {
  protected final GuiScreen previousScreen;

  private GuiButton loginButton = null;
  private GuiButton deleteButton = null;
  private GuiButton cancelButton = null;
  private GuiAccountList guiAccountList = null;
  public static Notification notification = null;
  private int selectedAccount = -1;
  private ExecutorService executor = null;
  private CompletableFuture<Void> task = null;

  public GuiAccountManager(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  public GuiAccountManager(GuiScreen previousScreen, Notification notification) {
    this.previousScreen = previousScreen;
    this.notification = notification;
  }

  @Override
  public void initGui() {
    AccountManager.load();
    Keyboard.enableRepeatEvents(true);

    buttonList.clear();
    buttonList.add(loginButton = new GuiButton(
            0, width / 2 - 150 - 4, height - 52, 150, 20, "Login"
    ));
    buttonList.add(new GuiButton(
            1, width / 2 + 4, height - 52, 150, 20, "Add"
    ));
    buttonList.add(deleteButton = new GuiButton(
            2, width / 2 - 150 - 4, height - 28, 150, 20, "Delete"
    ));
    buttonList.add(cancelButton = new GuiButton(
            3, width / 2 + 4, height - 28, 150, 20, "Cancel"
    ));

    guiAccountList = new GuiAccountList(mc);
    guiAccountList.registerScrollButtons(11, 12);

    updateScreen();
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);

    if (task != null && !task.isDone()) {
      task.cancel(true);
      executor.shutdownNow();
    }
  }

  @Override
  public void updateScreen() {
    if (loginButton != null && deleteButton != null) {
      loginButton.enabled = deleteButton.enabled = selectedAccount >= 0;
      if (task != null && !task.isDone()) {
        loginButton.enabled = false;
      }
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
    if (guiAccountList != null) {
      guiAccountList.drawScreen(mouseX, mouseY, renderPartialTicks);
    }
    super.drawScreen(mouseX, mouseY, renderPartialTicks);

    drawCenteredString(
            fontRendererObj,
            TextFormatting.translate(String.format(
                    "&rAccount Manager &8(&7%s&8)&r", AccountManager.accounts.size()
            )),
            width / 2, 20, -1
    );

    String text = TextFormatting.translate(String.format(
            "&7Username: &3%s&r", SessionManager.get().getUsername()
    ));
    mc.currentScreen.drawString(mc.fontRendererObj, text, 3, 3, -1);

    if (notification != null && !notification.isExpired()) {
      String notificationText = notification.getMessage();
      Gui.drawRect(
              mc.currentScreen.width / 2 - mc.fontRendererObj.getStringWidth(notificationText) / 2 - 3, 4,
              mc.currentScreen.width / 2 + mc.fontRendererObj.getStringWidth(notificationText) / 2 + 3, 4 + 3 + mc.fontRendererObj.FONT_HEIGHT + 2,
              0x64000000
      );
      mc.currentScreen.drawCenteredString(
              mc.fontRendererObj, notification.getMessage(),
              mc.currentScreen.width / 2, 4 + 3, -1
      );
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    if (guiAccountList != null) {
      guiAccountList.handleMouseInput();
    }
    super.handleMouseInput();
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    switch (keyCode) {
      case Keyboard.KEY_UP: {
        if (selectedAccount > 0) {
          --selectedAccount;
          if (isCtrlKeyDown()) {
            Collections.swap(AccountManager.accounts, selectedAccount, selectedAccount + 1);
            AccountManager.save();
          }
        }
      }
      break;
      case Keyboard.KEY_DOWN: {
        if (selectedAccount < AccountManager.accounts.size() - 1) {
          ++selectedAccount;
          if (isCtrlKeyDown()) {
            Collections.swap(AccountManager.accounts, selectedAccount, selectedAccount - 1);
            AccountManager.save();
          }
        }
      }
      break;
      case Keyboard.KEY_RETURN: {
        actionPerformed(loginButton);
      }
      break;
      case Keyboard.KEY_DELETE: {
        actionPerformed(deleteButton);
      }
      break;
      case Keyboard.KEY_ESCAPE: {
        actionPerformed(cancelButton);
      }
      break;
    }

    if (isKeyComboCtrlC(keyCode) && selectedAccount >= 0) {
      setClipboardString(AccountManager.accounts.get(selectedAccount).getUsername());
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null) {
      return;
    }

    if (button.enabled) {
      switch (button.id) {
        case 0: { // Login
          if (task == null || task.isDone()) {
            if (executor == null) {
              executor = Executors.newSingleThreadExecutor();
            }
            Account account = AccountManager.accounts.get(selectedAccount);
            String username = StringUtils.isBlank(account.getUsername()) ? "???" : account.getUsername();
            if (account.getType() == AccountType.CRACKED) {
              boolean loginSuccess = CrackedAuth.login(account.getUsername());
              if (loginSuccess) {
                notification = new Notification(TextFormatting.translate(String.format(
                        "&aSuccessful login! (%s)&r", account.getUsername()
                )), 5000L);
              } else {
                notification = new Notification(TextFormatting.translate(String.format(
                        "&cFailed to log in! (%s)&r", account.getUsername()
                )), 5000L);
              }
              return;
            }

            // Handle premium accounts
            notification = new Notification(TextFormatting.translate(String.format(
                    "&7Fetching your Minecraft profile... (%s)&r", username
            )), -1L);
            task = MicrosoftAuth.login(account.getAccessToken(), executor)
                    .handle((session, error) -> {
                      if (session != null) {
                        account.setUsername(session.getUsername());
                        AccountManager.save();
                        SessionManager.set(session);
                        notification = new Notification(TextFormatting.translate(String.format(
                                "&aSuccessful login! (%s)&r", account.getUsername()
                        )), 5000L);
                        return true;
                      }
                      return false;
                    })
                    .thenComposeAsync(completed -> {
                      if (completed) {
                        throw new NoSuchElementException();
                      }
                      notification = new Notification(TextFormatting.translate(String.format(
                              "&7Refreshing Microsoft access tokens... (%s)&r", username
                      )), -1L);
                      return MicrosoftAuth.refreshMSAccessTokens(account.getRefreshToken(), executor);
                    })
                    .thenComposeAsync(msAccessTokens -> {
                      notification = new Notification(TextFormatting.translate(String.format(
                              "&7Acquiring Xbox access token... (%s)&r", username
                      )), -1L);
                      return MicrosoftAuth.acquireXboxAccessToken(msAccessTokens.get("access_token"), executor);
                    })
                    .thenComposeAsync(xboxAccessToken -> {
                      notification = new Notification(TextFormatting.translate(String.format(
                              "&7Acquiring Xbox XSTS token... (%s)&r", username
                      )), -1L);
                      return MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor);
                    })
                    .thenComposeAsync(xboxXstsData -> {
                      notification = new Notification(TextFormatting.translate(String.format(
                              "&7Acquiring Minecraft access token... (%s)&r", username
                      )), -1L);
                      return MicrosoftAuth.acquireMCAccessToken(
                              xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor
                      );
                    })
                    .thenComposeAsync(mcToken -> {
                      notification = new Notification(TextFormatting.translate(String.format(
                              "&7Fetching your Minecraft profile... (%s)&r", username
                      )), -1L);
                      return MicrosoftAuth.login(mcToken, executor);
                    })
                    .thenAccept(session -> {
                      account.setUsername(session.getUsername());
                      AccountManager.save();
                      SessionManager.set(session);
                      notification = new Notification(TextFormatting.translate(String.format(
                              "&aSuccessful login! (%s)&r", account.getUsername()
                      )), 5000L);
                    })
                    .exceptionally(error -> {
                      if (!(error.getCause() instanceof NoSuchElementException)) {
                        notification = new Notification(TextFormatting.translate(String.format(
                                "&c%s (%s)&r", error.getMessage(), username
                        )), 5000L);
                      }
                      return null;
                    });
          }
        }
        break;
        case 1: { // Add
          mc.displayGuiScreen(new GuiAddAccount(previousScreen));
        }
        break;
        case 2: { // Delete
          if (selectedAccount > -1 && selectedAccount < AccountManager.accounts.size()) {
            AccountManager.accounts.remove(selectedAccount);
            AccountManager.save();
            selectedAccount = -1;
            updateScreen();
          }
        }
        break;
        case 3: { // Cancel
          mc.displayGuiScreen(new GuiMultiplayer(previousScreen));
        }
        break;

        default: {
          guiAccountList.actionPerformed(button);
        }
      }
    }
  }

  class GuiAccountList extends GuiSlot {
    public GuiAccountList(Minecraft mc) {
      super(
              mc, GuiAccountManager.this.width, GuiAccountManager.this.height,
              32, GuiAccountManager.this.height - 64, 16
      );
    }

    @Override
    protected int getSize() {
      return AccountManager.accounts.size();
    }

    @Override
    protected boolean isSelected(int slotIndex) {
      return slotIndex == GuiAccountManager.this.selectedAccount;
    }

    @Override
    protected int getScrollBarX() {
      return (this.width + getListWidth()) / 2 + 2;
    }

    @Override
    public int getListWidth() {
      return (150 + 4) * 2;
    }

    @Override
    protected int getContentHeight() {
      return AccountManager.accounts.size() * 16;
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      GuiAccountManager.this.selectedAccount = slotIndex;
      GuiAccountManager.this.updateScreen();
      if (isDoubleClick) {
        GuiAccountManager.this.actionPerformed(loginButton);
      }
    }

    @Override
    protected void drawBackground() {
      GuiAccountManager.this.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int entryID, int x, int y, int k, int mouseXIn, int mouseYIn) {
      FontRenderer fr = GuiAccountManager.this.fontRendererObj;
      Account account = AccountManager.accounts.get(entryID);

      String username = account.getUsername();
      if (StringUtils.isBlank(username)) {
        username = "&7&l?";
      } else if (username.equals(SessionManager.get().getUsername())) {
        username = String.format("&a&l%s", username);
      }
      String accountTypeSuffix = account.getType() == AccountType.CRACKED
              ? " &7(Cracked)"
              : " &7(Premium)";

      String translatedUsername = TextFormatting.translate(String.format("&r%s", username));
      String translatedSuffix = TextFormatting.translate(accountTypeSuffix);

      GuiAccountManager.this.drawString(fr, translatedUsername, x + 2, y + 2, -1);
      GuiAccountManager.this.drawString(fr, translatedSuffix, x + 2 + fr.getStringWidth(translatedUsername), y + 2, -1);


      long currentTime = System.currentTimeMillis();
      long unbanTime = account.getUnban();
      String unban;
      if (unbanTime < 0L) {
        unban = "&4&l⚠";
      } else if (unbanTime <= currentTime) {
        unban = "&2&l✔";
      } else {
        long diff = unbanTime - currentTime;
        long s = (diff / 1000L) % 60L;
        long m = (diff / 60000L) % 60L;
        long h = (diff / 3600000L) % 24L;
        long d = (diff / 86400000L);
        unban = String.format(
                "%s%s%s%s",
                d > 0L ? String.format("%dd", d) : "",
                h > 0L ? String.format(" %dh", h) : "",
                m > 0L ? String.format(" %dm", m) : "",
                s > 0L ? String.format(" %ds", s) : ""
        );
        unban = unban.trim();
        unban = String.format("%s &c&l⚠", unban);
      }
      unban = TextFormatting.translate(
              String.format("&r%s&r", unban)
      );
      GuiAccountManager.this.drawString(
              fr, unban, x + getListWidth() - 5 - fr.getStringWidth(unban), y + 2, -1
      );
    }
  }
}