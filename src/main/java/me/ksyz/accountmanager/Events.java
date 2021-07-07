package me.ksyz.accountmanager;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Events {
  @SubscribeEvent
  public void onTick(TickEvent.RenderTickEvent t) {
    GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
    if (guiScreen instanceof GuiMultiplayer || guiScreen instanceof GuiSelectWorld
        || guiScreen instanceof GuiAccountManager) {
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      guiScreen.drawString(Minecraft.getMinecraft().fontRendererObj,
          TextFormatting.GRAY + "" + TextFormatting.BOLD + "Username" + TextFormatting.RESET, 12, 16, -1);
      GL11.glPopMatrix();

      guiScreen.drawString(Minecraft.getMinecraft().fontRendererObj,
          TextFormatting.DARK_AQUA + Minecraft.getMinecraft().getSession().getUsername() + TextFormatting.RESET, 6, 12,
          -1);
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
    if ((event.gui instanceof GuiMultiplayer || event.gui instanceof GuiSelectWorld) && event.button.id == 69) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiAccountManager(event.gui));
    }
  }
}
