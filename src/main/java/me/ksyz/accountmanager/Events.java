package me.ksyz.accountmanager;

import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.gui.GuiAccountManager;
import me.ksyz.accountmanager.gui.GuiMicrosoftAuth;
import me.ksyz.accountmanager.utils.Notification;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.StringUtils;

public class Events {
  private static final Minecraft mc = Minecraft.getMinecraft();

  @SubscribeEvent
  public void onTick(final TickEvent.RenderTickEvent event) {
    if (mc.currentScreen == null) {
      return;
    }

    // Current username
    if (
      mc.currentScreen instanceof GuiSelectWorld ||
      mc.currentScreen instanceof GuiMultiplayer ||
      mc.currentScreen instanceof GuiAccountManager
    ) {
      GlStateManager.disableLighting();
      final String label = TextFormatting.translate("&r&7&lUsername&r");
      final String username = TextFormatting.translate(String.format(
        "&r&3%s&r", SessionManager.getSession().getUsername()
      ));
      final int width = Math.max(
        mc.fontRendererObj.getStringWidth(label) / 2,
        mc.fontRendererObj.getStringWidth(username)
      );
      Gui.drawRect(
        9 - 3, 9 - 3, 9 + width + 3, 9 + 14 + 3,
        0x70000000
      );
      Gui.drawRect(
        9 - 3, 9 - 3, 9 + width + 3, 9 - 3 + 1,
        0x70000000
      );
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5, 0.5, 1.0);
      mc.currentScreen.drawString(
        mc.fontRendererObj, label,
        9 * 2, 9 * 2, -1
      );
      GlStateManager.popMatrix();
      mc.currentScreen.drawString(
        mc.fontRendererObj, username,
        9, 9 + 5, -1
      );
      GlStateManager.enableLighting();
    }

    // Notification
    if (
      mc.currentScreen instanceof GuiAccountManager ||
      mc.currentScreen instanceof GuiMicrosoftAuth
    ) {
      final String notificationText = Notification.getNotificationText();
      if (!StringUtils.isBlank(notificationText)) {
        GlStateManager.disableLighting();
        mc.currentScreen.drawCenteredString(
          mc.fontRendererObj, notificationText,
          mc.currentScreen.width / 2, 7, Notification.getColor()
        );
        GlStateManager.enableLighting();
      }
    }
  }

  @SubscribeEvent
  public void initGuiEvent(final InitGuiEvent.Post event) {
    if (event.gui instanceof GuiSelectWorld || event.gui instanceof GuiMultiplayer) {
      event.buttonList.add(new GuiButton(
        69, event.gui.width - 106, 6, 100, 20, "Accounts"
      ));
    }
  }

  @SubscribeEvent
  public void onClick(final ActionPerformedEvent event) {
    if (event.gui instanceof GuiSelectWorld || event.gui instanceof GuiMultiplayer) {
      if (event.button.id == 69) {
        mc.displayGuiScreen(new GuiAccountManager(event.gui, true));
      }
    }
  }
}
