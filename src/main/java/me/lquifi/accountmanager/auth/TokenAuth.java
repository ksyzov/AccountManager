package me.lquifi.accountmanager.auth;

import me.lquifi.accountmanager.AccountManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.util.concurrent.*;

public class TokenAuth {

    private static final Executor executor = Executors.newCachedThreadPool();

    public static Session loginSession(String mcAccessToken) {
        if (mcAccessToken == null || mcAccessToken.trim().isEmpty()) {
            return null;
        }
        try {
            Session session = MicrosoftAuth.login(mcAccessToken, executor).get(30, TimeUnit.SECONDS);
            if (session == null) return null;
            setMinecraftSession(session);
            SessionManager.set(session);
            return session;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addAccount(String mcAccessToken) {
        Session session = loginSession(mcAccessToken);
        if (session == null) return false;

        AccountManager.accounts.add(new Account(
                "",
                mcAccessToken,
                session.getUsername(),
                System.currentTimeMillis()
        ));
        AccountManager.save();
        return true;
    }


    private static void setMinecraftSession(Session session) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            java.lang.reflect.Field sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
