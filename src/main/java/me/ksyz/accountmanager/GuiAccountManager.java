package me.ksyz.accountmanager;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.mojang.authlib.exceptions.AuthenticationException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.ResourceLocation;

public class GuiAccountManager extends GuiScreen {
  private GuiAccountManager.List guiAccountList;
  private GuiNotification guiNotification;
  private GuiButton loginButton;
  private GuiButton editButton;
  private GuiButton deleteButton;

  private final GuiScreen previousScreen;
  private int selectedAccount = -1;

  GuiAccountManager(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    this.buttonList.clear();

    // Top Row
    this.buttonList.add(loginButton = new GuiButton(0, this.width / 2 - 154 - 10, this.height - 52, 120, 20, "Login"));
    this.buttonList.add(editButton = new GuiButton(1, this.width / 2 - 40, this.height - 52, 80, 20, "Edit"));
    this.buttonList.add(new GuiButton(2, this.width / 2 + 4 + 40, this.height - 52, 120, 20, "Add"));

    // Bottom Row
    this.buttonList.add(new GuiButton(3, this.width / 2 - 154 - 10, this.height - 28, 110, 20, "Import"));
    this.buttonList.add(deleteButton = new GuiButton(4, this.width / 2 - 50, this.height - 28, 100, 20, "Delete"));
    this.buttonList.add(new GuiButton(5, this.width / 2 + 4 + 50, this.height - 28, 110, 20, "Cancel"));

    // Notification
    guiNotification = new GuiNotification();

    // Account List
    guiAccountList = new GuiAccountManager.List(this.mc);
    guiAccountList.registerScrollButtons(6, 7);
    updateButtons();
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    this.guiAccountList.handleMouseInput();
  }

  @Override
  public void updateScreen() {
    updateButtons();
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    AccountManager.getInstance().save();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
    guiAccountList.drawScreen(mouseX, mouseY, renderPartialTicks);
    super.drawScreen(mouseX, mouseY, renderPartialTicks);

    // Notification
    String text = guiNotification.getNotificationText();
    if (!"".equals(text)) {
      this.drawCenteredString(fontRendererObj, text, this.width / 2, 7, guiNotification.getColor());
    }

    // Action message
    this.drawCenteredString(fontRendererObj,
        TextFormatting.RESET + "Account Manager " + TextFormatting.DARK_GRAY + "(" + TextFormatting.GRAY
            + AccountManager.getInstance().getAccounts().size() + TextFormatting.DARK_GRAY + ")" + TextFormatting.RESET,
        this.width / 2, 20, -1);

//    // Current username message
//    this.drawCenteredString(Minecraft.getMinecraft().fontRendererObj,
//        TextFormatting.ITALIC + "You are currently logged in as " + TextFormatting.BOLD
//            + Minecraft.getMinecraft().getSession().getUsername() + TextFormatting.RESET,
//        this.width / 2, this.height - 62, -1);
  }

  @Override
  protected void keyTyped(char character, int keyIndex) {
    switch (keyIndex) {
    case Keyboard.KEY_UP:
      if (selectedAccount > 0) {
        --selectedAccount;
      }
      break;
    case Keyboard.KEY_DOWN:
      if (selectedAccount < AccountManager.getInstance().getAccounts().size() - 1) {
        ++selectedAccount;
      }
      break;
    case Keyboard.KEY_RETURN:
      if (loginButton.enabled) {
        login();
      }
      break;
    case Keyboard.KEY_DELETE:
      if (deleteButton.enabled) {
        deleteAccount();
      }
      break;
    case Keyboard.KEY_ESCAPE:
      cancel();
      break;
    }

    if (GuiScreen.isKeyComboCtrlC(keyIndex) && selectedAccount >= 0) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      String email = AccountManager.getInstance().getAccounts().get(selectedAccount).getEmail();
      String password = AccountManager.getInstance().getAccounts().get(selectedAccount).getPassword();
      if ("".equals(password)) {
        // Copy only name (email)
        clipboard.setContents(new StringSelection(email), null);
      } else {
        // Copy email:password
        clipboard.setContents(new StringSelection(email + ":" + password), null);
      }
    } else if (GuiScreen.isKeyComboCtrlV(keyIndex)) {
      importAccounts();
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.enabled) {
      switch (button.id) {
      case 0:
        login();
        break;
      case 1:
        editAccount();
        break;
      case 2:
        addAccount();
        break;
      case 3:
        importAccounts();
        break;
      case 4:
        deleteAccount();
        break;
      case 5:
        cancel();
        break;
      default:
        guiAccountList.actionPerformed(button);
      }
    }
  }

  // Attempts login to the account, then returns to the previous menu if
  // successful
  private void login() {
    Account acc = AccountManager.getInstance().getAccounts().get(selectedAccount);
    String name = acc.getName();
    if ("".equals(name)) {
      name = acc.getEmail();
    }

    try {
      AccountManager.getInstance().login(acc);
      guiNotification.setNotification("Successful login! (" + name + ")", -11141291);
      Minecraft.getMinecraft().getSoundHandler()
          .playSound(PositionedSoundRecord.create(new ResourceLocation("note.pling")));
    } catch (AuthenticationException e) {
      guiNotification.setNotification("Invalid credentials! (" + name + ")", -43691);
      Minecraft.getMinecraft().getSoundHandler()
          .playSound(PositionedSoundRecord.create(new ResourceLocation("note.bass")));
    }
  }

  // Edits the current acccount's information
  private void editAccount() {
    if (selectedAccount >= 0) {
      mc.displayGuiScreen(new GuiEdit(previousScreen, selectedAccount));
    }
  }

  // Adds an account
  private void addAccount() {
    mc.displayGuiScreen(new GuiAdd(previousScreen));
  }

  // Imports accounts from clipboard
  private void importAccounts() {
    String clipboardData = "";
    try {
      clipboardData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
      e.printStackTrace();
    }

    String[] lines = clipboardData.split("\\r?\\n");
    for (String line : lines) {
      if (line.contains(":")) {
        String[] combo = line.split(":");
        Account acc = AccountManager.getInstance().getAccountToAdd(combo[0], combo[1]);
        AccountManager.getInstance().getAccounts().add(acc);
      }
    }
  }

  // Deletes the selected account
  private void deleteAccount() {
    AccountManager.getInstance().getAccounts().remove(selectedAccount);
    --selectedAccount;
    updateButtons();
  }

  // Returns to the previous menu
  private void cancel() {
    mc.displayGuiScreen(previousScreen);
  }

  private void updateButtons() {
    loginButton.enabled = selectedAccount >= 0 && !Minecraft.getMinecraft().getSession().getUsername()
        .equals(AccountManager.getInstance().getAccounts().get(selectedAccount).getName());
    editButton.enabled = selectedAccount >= 0;
    deleteButton.enabled = selectedAccount >= 0;
  }

  class List extends GuiSlot {
    public List(Minecraft mc) {
      super(mc, GuiAccountManager.this.width, GuiAccountManager.this.height, 32, GuiAccountManager.this.height - 64,
          27);
    }

    @Override
    protected int getSize() {
      return AccountManager.getInstance().getAccounts().size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      GuiAccountManager.this.selectedAccount = slotIndex;
      GuiAccountManager.this.updateButtons();

      if (isDoubleClick) {
        GuiAccountManager.this.login();
      }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
      return slotIndex == GuiAccountManager.this.selectedAccount;
    }

    @Override
    protected int getContentHeight() {
      return AccountManager.getInstance().getAccounts().size() * 27;
    }

    @Override
    protected void drawBackground() {
      GuiAccountManager.this.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn,
        int mouseYIn) {
      String name = AccountManager.getInstance().getAccounts().get(entryID).getName();
      String email = AccountManager.getInstance().getAccounts().get(entryID).getEmail();
      String password = AccountManager.getInstance().getAccounts().get(entryID).getPassword();
      int color = -1;

      if ("".equals(name)) {
        name = "???";
      } else if (Minecraft.getMinecraft().getSession().getUsername().equals(name)) {
        name = name + TextFormatting.GREEN + " \u2714" + TextFormatting.RESET;
      }

      String combo = "";
      if ("".equals(password)) {
        combo = TextFormatting.DARK_GRAY + "Offline" + TextFormatting.RESET;
      } else {
        combo = TextFormatting.GRAY + email + TextFormatting.DARK_GRAY /* + ":" + TextFormatting.GRAY + "***" */
            + TextFormatting.RESET;
      }

      GuiAccountManager.this.drawString(GuiAccountManager.this.fontRendererObj, name, p_180791_2_ + 2, p_180791_3_ + 2,
          color);
      GuiAccountManager.this.drawString(GuiAccountManager.this.fontRendererObj, combo, p_180791_2_ + 2,
          p_180791_3_ + 13, -1);
    }
  }
}
