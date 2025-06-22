package me.ksyz.accountmanager.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLUtils {
  private static final SSLContext ctx;

  public static SSLContext getSSLContext() {
    return ctx;
  }

  static {
    try {
      // Load keystore from resources
      KeyStore jks = KeyStore.getInstance("JKS");
      InputStream stream = SSLUtils.class.getResourceAsStream("/ssl.jks");
      if (stream == null) {
        throw new RuntimeException("Couldn't find ssl.jks in resources");
      }
      jks.load(stream, "changeit".toCharArray());

      // Initialize TrustManagerFactory with the keystore
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(jks);

      // Set up the SSL context with the trust managers from the keystore
      ctx = SSLContext.getInstance("TLS");
      ctx.init(null, tmf.getTrustManagers(), null);

      // Set as the default SSL socket factory
      HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize custom SSLContext", e);
    }
  }
}
