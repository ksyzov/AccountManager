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
            // Fetch Minecraft profile asynchronously and wait for result (timeout after 30 sec)
            Session session = MicrosoftAuth.login(mcAccessToken, executor).get(30, TimeUnit.SECONDS);

            if (session == null) {
                System.err.println("Failed to fetch Minecraft session profile.");
                return false;
            }

            // Set Minecraft session via reflection
            setMinecraftSession(session);

            // Set in your SessionManager (if applicable)
            SessionManager.set(session);

            System.out.println("Successfully logged in with access token for user: " + session.getUsername());
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
