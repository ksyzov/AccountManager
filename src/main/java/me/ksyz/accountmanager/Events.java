package me.ksyz.accountmanager;

import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.gui.GuiAccountManager;
import me.ksyz.accountmanager.gui.GuiMicrosoftAuth;
import me.ksyz.accountmanager.utils.Notification;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
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
  public void onTick(TickEvent.RenderTickEvent t) {
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
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5, 0.5, 1.0);
      mc.currentScreen.drawString(
        mc.fontRendererObj,
        TextFormatting.translate("&7&lUsername&r"),
        12, 16, -1
      );
      GlStateManager.popMatrix();
      mc.currentScreen.drawString(
        mc.fontRendererObj,
        TextFormatting.translate(String.format("&3%s&r", SessionManager.getSession().getUsername())),
        6, 12, -1
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
          mc.fontRendererObj,
          Notification.getNotificationText(),
          mc.currentScreen.width / 2, 7, Notification.getColor()
        );
        GlStateManager.enableLighting();
      }
    }
  }

  @SubscribeEvent
  public void initGuiEvent(InitGuiEvent.Post event) {
    if (event.gui instanceof GuiMultiplayer || event.gui instanceof GuiSelectWorld) {
      event.buttonList.add(new GuiButton(
        69, event.gui.width - 106, 6, 100, 20, "Accounts"
      ));
    }
  }

  @SubscribeEvent
  public void onClick(ActionPerformedEvent event) {
    if (event.gui instanceof GuiMultiplayer || event.gui instanceof GuiSelectWorld) {
      if (event.button.id == 69) {
        mc.displayGuiScreen(new GuiAccountManager(event.gui));
      }
    }
  }
}
