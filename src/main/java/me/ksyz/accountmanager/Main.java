package me.ksyz.accountmanager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
  private static final AccountManager am = AccountManager.getAccountManager();

  public static final String MODID = "accountmanager";
  public static final String VERSION = "1.0";

  @EventHandler
  public static void init(FMLInitializationEvent event) {
    try {
      // The password is used for encryption and is not really important
      am.setPassword("5d2ef462ec9fccd8756aacbf865c7a65");
      am.read();
    } catch (Exception e) {
      System.err.println("Password for Account Manager is incorrect!");
    }

    MinecraftForge.EVENT_BUS.register(new Events());
  }
}
