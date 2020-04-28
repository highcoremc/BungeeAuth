package ru.yooxa.bungee.auth.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import ru.yooxa.bungee.auth.Main;


public class PasswordSecurity
{
  private static SecureRandom rnd = new SecureRandom();
  
  public static String createSalt(int length) throws NoSuchAlgorithmException {
    byte[] msg = new byte[40];
    rnd.nextBytes(msg);
    MessageDigest sha1 = MessageDigest.getInstance("SHA1");
    sha1.reset();
    byte[] digest = sha1.digest(msg);
    return String.format("%0" + (digest.length << 1) + "x", new Object[] { new BigInteger(1, digest) }).substring(0, length);
  } public static String getHash(String password, String playerName) throws NoSuchAlgorithmException {
    String salt;
    EncryptionMethod method;
    try {
      method = (EncryptionMethod)Main.getAlgorithm().getСlass().newInstance();
    } catch (InstantiationException|IllegalAccessException e) {
      throw new NoSuchAlgorithmException("Problem with this hash algorithm");
    } 
    
    if (method == null) {
      throw new NoSuchAlgorithmException("Unknown hash algorithm");
    }
    
    switch (Main.getAlgorithm()) {
      case SHA256:
        salt = createSalt(16);
        return method.getHash(password, salt, playerName);
      
      case MD5:
        return method.getHash(password, "", playerName);
      
      case XAUTH:
        salt = createSalt(12);
        return method.getHash(password, salt, playerName);
    } 
    
    throw new NoSuchAlgorithmException("Unknown hash algorithm");
  }

  
  public static boolean comparePasswordWithHash(String password, String hash, String playerName) throws NoSuchAlgorithmException {
    EncryptionMethod method;
    try {
      method = (EncryptionMethod)Main.getAlgorithm().getСlass().newInstance();
    } catch (InstantiationException|IllegalAccessException e) {
      throw new NoSuchAlgorithmException("Problem with this hash algorithm");
    } 
    
    if (method == null) {
      throw new NoSuchAlgorithmException("Unknown hash algorithm");
    }
    try {
      if (method.comparePassword(hash, password, playerName)) {
        return true;
      }
    } catch (Exception exception) {}


    
    return false;
  }
}


