package me.ksyz.accountmanager;

import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.gui.GuiAccountManager;
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

public class Events {
  private static final Minecraft mc = Minecraft.getMinecraft();

  @SubscribeEvent
  public void onTick(TickEvent.RenderTickEvent event) {
    if (mc.currentScreen == null) {
      return;
    }

    if (mc.currentScreen instanceof GuiSelectWorld || mc.currentScreen instanceof GuiMultiplayer) {
      String text = TextFormatting.translate(String.format(
        "&7Username: &3%s&r", SessionManager.getSession().getUsername()
      ));
      GlStateManager.disableLighting();
      mc.currentScreen.drawString(mc.fontRendererObj, text, 3, 3, -1);
      GlStateManager.enableLighting();
    }
  }

  @SubscribeEvent
  public void initGuiEvent(InitGuiEvent.Post event) {
    if (event.gui instanceof GuiSelectWorld || event.gui instanceof GuiMultiplayer) {
      event.buttonList.add(new GuiButton(
        69, event.gui.width - 106, 6, 100, 20, "Accounts"
      ));
    }
  }

  @SubscribeEvent
  public void onClick(ActionPerformedEvent event) {
    if (event.gui instanceof GuiSelectWorld || event.gui instanceof GuiMultiplayer) {
      if (event.button.id == 69) {
        mc.displayGuiScreen(new GuiAccountManager(event.gui));
      }
    }
  }
}
