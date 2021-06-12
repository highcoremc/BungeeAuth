package me.loper.bungeeauth.authentication.hash;

public interface HashMethod {
    /**
     * Accept string value and returns hash from this value.
     *
     * @param entry String
     * @return String
     */
    String hash(String entry);

    /**
     * Verify string value with passed entry.
     *
     * @param entry String to be checked.
     * @param actual String with the actual hashed value.
     *
     * @return boolean
     */
    boolean verify(String entry, String actual);
}
