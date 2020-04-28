package ru.yooxa.bungee.auth.hash;

import java.security.NoSuchAlgorithmException;

public interface EncryptionMethod {
    String getHash(String paramString1, String paramString2, String paramString3) throws NoSuchAlgorithmException;

    boolean comparePassword(String paramString1, String paramString2, String paramString3) throws NoSuchAlgorithmException;
}


