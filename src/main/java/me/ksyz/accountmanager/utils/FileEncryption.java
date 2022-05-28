package me.ksyz.accountmanager.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileEncryption {
  private static byte[] salt, iv;

  public static void encrypt(final String password, final JsonArray json) throws Exception {
    if (salt == null || iv == null) {
      final SecureRandom rdm = new SecureRandom();
      salt = new byte[64];
      iv = new byte[16];
      rdm.nextBytes(salt);
      rdm.nextBytes(iv);
    }
    final Cipher cipher = create(password, true);
    final File out = new File(Minecraft.getMinecraft().mcDataDir, "accounts.enc");
    final FileOutputStream fos = new FileOutputStream(out);
    fos.write(salt);
    fos.write(iv);
    fos.write(cipher.doFinal(json.toString().getBytes(StandardCharsets.UTF_8)));
    fos.close();
  }

  public static JsonArray decrypt(final String password) throws Exception {
    final File in = new File(Minecraft.getMinecraft().mcDataDir, "accounts.enc");
    final FileInputStream fis = new FileInputStream(in);
    salt = new byte[64];
    iv = new byte[16];
    fis.read(salt);
    fis.read(iv);
    final byte[] encrypted = new byte[(int) in.length() - (64 + 16)];
    fis.read(encrypted);
    fis.close();
    final Cipher cipher = create(password, false);
    return new JsonParser().parse(IOUtils.toString(cipher.doFinal(encrypted), "UTF-8")).getAsJsonArray();
  }

  private static Cipher create(final String password, final boolean encrypt) throws Exception {
    final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    final KeySpec passwordBasedEncryptionKeySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
    final SecretKey secretKeyFromPBKDF2 = secretKeyFactory.generateSecret(passwordBasedEncryptionKeySpec);
    final SecretKey key = new SecretKeySpec(secretKeyFromPBKDF2.getEncoded(), "AES");
    final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    final IvParameterSpec spec = new IvParameterSpec(iv);
    cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key, spec);
    return cipher;
  }
}
