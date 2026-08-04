package me.ksyz.accountmanager.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;

public class GuiMaskedTextField extends GuiTextField {
  private final FontRenderer fontRenderer;
  private int cursorCounter;
  private boolean enabled = true;

  public GuiMaskedTextField(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height) {
    super(componentId, fontRenderer, x, y, width, height);
    this.fontRenderer = fontRenderer;
  }

  @Override
  public void updateCursorCounter() {
    super.updateCursorCounter();
    ++cursorCounter;
  }

  @Override
  public void setFocused(boolean focused) {
    super.setFocused(focused);
    if (focused) {
      cursorCounter = 0;
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    this.enabled = enabled;
  }

  public void drawMaskedTextBox() {
    if (!getVisible()) {
      return;
    }

    if (getEnableBackgroundDrawing()) {
      Gui.drawRect(xPosition - 1, yPosition - 1, xPosition + width + 1, yPosition + height + 1, -6250336);
      Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, -16777216);
    }

    String text = getText();
    String maskedText = StringUtils.repeat("*", text.length());
    int availableWidth = getWidth();
    int characterWidth = Math.max(1, fontRenderer.getStringWidth("*"));
    int maxVisibleCharacters = Math.max(1, availableWidth / characterWidth);
    int cursorPosition = Math.max(0, Math.min(getCursorPosition(), text.length()));
    int start = Math.max(0, cursorPosition - maxVisibleCharacters + 1);
    start = Math.min(start, Math.max(0, text.length() - maxVisibleCharacters));
    int end = Math.min(text.length(), start + maxVisibleCharacters);
    String visibleText = maskedText.substring(start, end);
    int visibleCursorPosition = Math.max(0, Math.min(cursorPosition - start, visibleText.length()));
    int textColor = enabled ? 14737632 : 7368816;
    int textX = getEnableBackgroundDrawing() ? xPosition + 4 : xPosition;
    int textY = getEnableBackgroundDrawing() ? yPosition + (height - 8) / 2 : yPosition;

    int selectionStart = Math.min(getCursorPosition(), getSelectionEnd());
    int selectionEnd = Math.max(getCursorPosition(), getSelectionEnd());
    int visibleSelectionStart = Math.max(selectionStart, start) - start;
    int visibleSelectionEnd = Math.min(selectionEnd, end) - start;
    if (visibleSelectionStart < visibleSelectionEnd) {
      int selectionX = textX + fontRenderer.getStringWidth(visibleText.substring(0, visibleSelectionStart));
      int selectionEndX = textX + fontRenderer.getStringWidth(visibleText.substring(0, visibleSelectionEnd));
      Gui.drawRect(selectionX, textY - 1, selectionEndX, textY + 1 + fontRenderer.FONT_HEIGHT, -16776961);
    }

    if (!visibleText.isEmpty()) {
      fontRenderer.drawStringWithShadow(visibleText, textX, textY, textColor);
    }

    boolean cursorVisible = isFocused() && cursorCounter / 6 % 2 == 0;
    if (cursorVisible) {
      int cursorX = textX + fontRenderer.getStringWidth(
        visibleText.substring(0, visibleCursorPosition)
      );
      Gui.drawRect(
        cursorX, textY - 1,
        cursorX + 1, textY + 1 + fontRenderer.FONT_HEIGHT,
        -3092272
      );
    }
  }
}
