package me.lquifi.accountmanager.auth;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.util.concurrent.*;

public class TokenAuth {

    private static final Executor executor = Executors.newCachedThreadPool();

    public static boolean login(String mcAccessToken) {
        if (mcAccessToken == null || mcAccessToken.trim().isEmpty()) {
            System.err.println("Access token cannot be null or empty!");
            return false;
        }

        try {
            Session session = MicrosoftAuth.login(mcAccessToken, executor).get(30, TimeUnit.SECONDS);

            if (session == null) {
                return false;
            }

            setMinecraftSession(session);
            SessionManager.set(session);
            return true;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void setMinecraftSession(Session session) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            java.lang.reflect.Field sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, session);
        } catch (Exception e) {
            System.err.println("Failed to set Minecraft session: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
