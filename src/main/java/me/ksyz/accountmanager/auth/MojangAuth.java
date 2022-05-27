package me.ksyz.accountmanager.auth;

import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.util.Session;

import java.net.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Utility methods for authenticating via Mojang.
 * Source: https://github.com/axieum/authme
 */
public final class MojangAuth {
  /**
   * Logs into Mojang and returns a new Minecraft session.
   *
   * <p>NB: You must manually interrupt the executor thread if the
   * completable future is cancelled!
   *
   * @param username Mojang username
   * @param password Mojang password
   * @param executor executor to run the login task on
   * @return completable future for the new Minecraft session
   */
  public static CompletableFuture<Session> login(
    final String username,
    final String password,
    final Executor executor
  ) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Create the Yggdrasil User Authentication provider
        final UserAuthentication yua = new YggdrasilAuthenticationService(
          Proxy.NO_PROXY, UUID.randomUUID().toString()
        ).createUserAuthentication(Agent.MINECRAFT);

        // Update the credentials and login
        yua.setUsername(username);
        yua.setPassword(password);
        yua.logIn();

        // Pluck all useful session data
        final String name = yua.getSelectedProfile().getName();
        final String uuid = UUIDTypeAdapter.fromUUID(yua.getSelectedProfile().getId());
        final String token = yua.getAuthenticatedToken();
        final String type = yua.getUserType().getName();

        // Logout after fetching what is needed
        yua.logOut();

        // Finally, return
        return new Session(name, uuid, token, type);
      } catch (Exception e) {
        throw new CompletionException(e);
      }
    }, executor);
  }
}
