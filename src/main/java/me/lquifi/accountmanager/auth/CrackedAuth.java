package me.lquifi.accountmanager.auth;

import me.lquifi.accountmanager.AccountManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import java.lang.reflect.Field;
import java.util.UUID;

public class CrackedAuth {

    public static boolean login(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username cannot be null or empty!");
            return false;
        }
        AccountManager.addCrackedAccount(username);
        String uuid = getUUID(username);
        Session session = new Session(username, uuid, "accessToken", "legacy");
        setMinecraftSession(session);
        SessionManager.set(session);


        System.out.println("Successfully logged in as: " + username);
        return true;
    }

    private static String getUUID(String name) {
        String offlinePlayerString = "OfflinePlayer:" + name;
        return UUID.nameUUIDFromBytes(offlinePlayerString.getBytes()).toString().replace("-", "");
    }

    private static void setMinecraftSession(Session session) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            Field sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, session);
        } catch (Exception e) {
            System.err.println("Failed to set Minecraft session: " + e.getMessage());
            e.printStackTrace();
        }
    }
}