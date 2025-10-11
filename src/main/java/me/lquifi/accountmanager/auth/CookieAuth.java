package me.lquifi.accountmanager.auth;

import com.google.gson.Gson;
import me.lquifi.accountmanager.AccountManager;
import me.lquifi.accountmanager.gui.GuiAccountManager;
import me.lquifi.accountmanager.gui.GuiCookieAuth;
import me.lquifi.accountmanager.utils.Notification;
import me.lquifi.accountmanager.utils.TextFormatting;
import net.minecraft.util.Session;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class CookieAuth {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Gson gson = new Gson();

    public static class McResponse {
        public String access_token;
    }

    public static class ProfileResponse {
        public String name;
        String id;
    }

    public static CompletableFuture<Boolean> addAccountFromCookieFile(File cookieFile, GuiCookieAuth gui) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        executor.execute(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(cookieFile), StandardCharsets.UTF_8))) {
                gui.status = "&fReading cookie file...&r";
                Map<String, String> cookieMap = new HashMap<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t", -1);
                    if (parts.length > 6 && parts[0].endsWith("login.live.com")) {
                        String name = parts[5].trim();
                        if (!cookieMap.containsKey(name)) {
                            cookieMap.put(name, parts[6].trim());
                        }
                    }
                }
                if (cookieMap.isEmpty()) {
                    gui.status = "&cNo valid login.live.com cookies found&r";
                    future.complete(false);
                    return;
                }
                gui.status = "&fBuilding cookie string...&r";
                String cookieString = buildCookieString(cookieMap);
                gui.status = "&fAuthenticating with Microsoft...&r";

                authenticateWithCookies(cookieString, gui, null).whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.complete(false);
                    } else {
                        future.complete(result);
                    }
                });
            } catch (Exception e) {
                gui.status = "&cError processing cookie file&r";
                e.printStackTrace();
                future.complete(false);
            }
        });

        return future;
    }



    public static String buildCookieString(Map<String, String> cookies) {
        StringBuilder sb = new StringBuilder();
        cookies.forEach((k, v) -> sb.append(k).append("=").append(v).append("; "));
        return sb.toString().replaceAll("; $", "");
    }

    public static String followRedirectChain(String cookieString) throws Exception {
        GuiAccountManager.notification = new Notification(TextFormatting.translate("&7Starting Microsoft authentication (1/3)..."), 5000L);
        String url1 = "https://sisu.xboxlive.com/connect/XboxLive/?state=login" +
                "&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d" +
                "&tid=896928775" +
                "&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin" +
                "&aid=1142970254";
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url1).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9," +
                "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en-US;q=0.8");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        String location1 = conn.getHeaderField("Location");
        if (location1 == null) {
            throw new Exception("Redirect failed at step 1");
        }
        location1 = location1.replaceAll(" ", "%20");
        conn.disconnect();

        GuiAccountManager.notification = new Notification(TextFormatting.translate("&7Processing Microsoft redirect (2/3)..."), 5000L);
        conn = (HttpsURLConnection) new URL(location1).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9," +
                "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
        conn.setRequestProperty("Cookie", cookieString);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        String location2 = conn.getHeaderField("Location");
        if (location2 == null) {
            throw new Exception("Redirect failed at step 2");
        }
        conn.disconnect();

        GuiAccountManager.notification = new Notification(TextFormatting.translate("&7Finalizing Microsoft redirect (3/3)..."), 5000L);
        conn = (HttpsURLConnection) new URL(location2).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9," +
                "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
        conn.setRequestProperty("Cookie", cookieString);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        String location3 = conn.getHeaderField("Location");
        if (location3 == null) {
            throw new Exception("Redirect failed at step 3");
        }
        conn.disconnect();
        return location3;
    }

    private static CompletableFuture<Boolean> authenticateWithCookies(String cookieString, GuiCookieAuth gui, String cookie) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                gui.status = "&fStarting authentication process...&r";
                String finalLocation = followRedirectChain(cookieString);

                gui.status = "&fExtracting access token from redirect...&r";
                String accessToken = finalLocation.split("accessToken=")[1];

                gui.status = "&fDecoding access token...&r";
                String decoded = new String(Base64.getDecoder().decode(accessToken), StandardCharsets.UTF_8);

                gui.status = "&fParsing token data...&r";
                String[] parts = decoded.split("\"rp://api.minecraftservices.com/\",");
                if (parts.length < 2) {
                    throw new Exception("Failed to decode token");
                }
                String rest = parts[1];

                gui.status = "&fExtracting XBL token components...&r";
                String token = rest.split("\"Token\":\"")[1].split("\"")[0];
                String uhs = rest.split(Pattern.quote("{\"DisplayClaims\":{\"xui\":[{\"uhs\":\""))[1].split("\"")[0];
                String xblToken = "XBL3.0 x=" + uhs + ";" + token;

                gui.status = "&fAuthenticating with Xbox Live...&r";
                McResponse mcRes = postMinecraftLogin(xblToken);

                if (mcRes == null || mcRes.access_token == null) {
                    System.err.println("[AuthFlow] Failed to get Minecraft access token");
                    gui.status = "&cFailed to get Minecraft access token&r";
                    return false;
                }

                gui.status = "&fRetrieving Minecraft profile...&r";
                ProfileResponse profileRes = getMinecraftProfile(mcRes.access_token);
                if (profileRes == null || profileRes.name == null) {
                    System.err.println("[AuthFlow] Failed to get Minecraft profile");
                    gui.status = "&cFailed to get Minecraft profile&r";
                    return false;
                }

                gui.status = "&aCreating Minecraft session...&r";
                Session session = new Session(
                        profileRes.name,
                        profileRes.id,
                        mcRes.access_token,
                        "legacy"
                );

                gui.status = "&aSaving account details...&r";
                AccountManager.accounts.add(new Account(
                        "",
                        mcRes.access_token,
                        session.getUsername(),
                        System.currentTimeMillis()
                ));

                AccountManager.save();
                SessionManager.set(session);
                System.out.println("[AuthFlow] Successfully logged in as " + session.getUsername());

                gui.status = "&aSuccessfully logged in as " + session.getUsername() + "&r";
                return true;
            } catch (Exception e) {
                System.err.println("[AuthFlow] Authentication failed: " + e.getMessage());
                gui.status = "&cInvalid Cookie File&r";
                return false;
            }
        });
    }


    public static McResponse postMinecraftLogin(String xblToken) throws Exception {
        GuiAccountManager.notification = new Notification(TextFormatting.translate("&7Logging into Minecraft services..."), 5000L);
        String url = "https://api.minecraftservices.com/authentication/login_with_xbox";
        String payload = "{\"identityToken\":\"" + xblToken + "\",\"ensureLegacyEnabled\":true}";
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        StringBuilder response = new StringBuilder();
        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return gson.fromJson(response.toString(), McResponse.class);
    }

    public static ProfileResponse getMinecraftProfile(String accessToken) throws Exception {
        GuiAccountManager.notification = new Notification(TextFormatting.translate("&7Fetching Minecraft profile..."), 5000L);
        String url = "https://api.minecraftservices.com/minecraft/profile";
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        StringBuilder response = new StringBuilder();
        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return gson.fromJson(response.toString(), ProfileResponse.class);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}