package me.ksyz.accountmanager.auth;

import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.ksyz.accountmanager.account.MojangAccount;
import net.minecraft.util.Session;

import java.net.Proxy;

public class MojangAuth {
  public static Session login(MojangAccount account) throws AuthenticationException {
    UserAuthentication auth = new YggdrasilAuthenticationService(
      Proxy.NO_PROXY, "1"
    ).createUserAuthentication(Agent.MINECRAFT);
    auth.setUsername(account.getEmail());
    auth.setPassword(account.getPassword());
    try {
      auth.logIn();
      account.setUsername(auth.getSelectedProfile().getName());
      return new Session(
        auth.getSelectedProfile().getName(),
        auth.getSelectedProfile().getId().toString().replace("-", ""),
        auth.getAuthenticatedToken(),
        Session.Type.MOJANG.toString()
      );
    } catch (AuthenticationException e) {
      throw new AuthenticationException("Login failed");
    }
  }
}
