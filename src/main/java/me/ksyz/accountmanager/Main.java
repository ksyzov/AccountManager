package me.ksyz.accountmanager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
  modid = Main.MODID, version = Main.VERSION,
  clientSideOnly = true, acceptedMinecraftVersions = "1.8.9"
)
public class Main {
  public static final String MODID = "accountmanager";
  public static final String VERSION = "@VERSION@";

  @EventHandler
  public static void init(final FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new Events());
    AccountManager.init();
    AccountManager.read();
  }
}
