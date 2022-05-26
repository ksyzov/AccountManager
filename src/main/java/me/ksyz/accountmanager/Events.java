package me.ksyz.accountmanager;

import me.ksyz.accountmanager.auth.SessionManager;
import me.ksyz.accountmanager.gui.GuiAccountManager;
import me.ksyz.accountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Events {
  private static final Minecraft mc = Minecraft.getMinecraft();

  @SubscribeEvent
  public void onTick(TickEvent.RenderTickEvent t) {
    GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
    if (
      guiScreen instanceof GuiMultiplayer ||
      guiScreen instanceof GuiSelectWorld ||
      guiScreen instanceof GuiAccountManager
    ) {
      GlStateManager.disableLighting();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5, 0.5, 1.0);
      guiScreen.drawString(
        mc.fontRendererObj,
        TextFormatting.GRAY + "" + TextFormatting.BOLD + "Username" + TextFormatting.RESET,
        12, 16, -1
      );
      GlStateManager.popMatrix();
      guiScreen.drawString(
        mc.fontRendererObj,
        TextFormatting.DARK_AQUA + SessionManager.getSession().getUsername() + TextFormatting.RESET,
        6, 12, -1
      );
      GlStateManager.enableLighting();
    }
  }

  @SubscribeEvent
  public void initGuiEvent(InitGuiEvent.Post event) {
    GuiScreen guiScreen = event.gui;
    if (guiScreen instanceof GuiMultiplayer || guiScreen instanceof GuiSelectWorld) {
      event.buttonList.add(new GuiButton(69, guiScreen.width - 106, 6, 100, 20, "Accounts"));
    }
  }

  @SubscribeEvent
  public void onClick(ActionPerformedEvent event) {
    if (event.gui instanceof GuiMultiplayer || event.gui instanceof GuiSelectWorld) {
      if (event.button.id == 69) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiAccountManager(event.gui));
      }
    }
  }
}
