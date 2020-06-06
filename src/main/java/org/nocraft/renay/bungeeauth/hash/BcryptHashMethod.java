package org.nocraft.renay.bungeeauth.hash;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptHashMethod implements HashMethod {
    @Override
    public String createHash(String entry) {
        return BCrypt.hashpw(entry, BCrypt.gensalt());
    }

    @Override
    public boolean verify(String entry, String actual) {
        return BCrypt.checkpw(entry, actual);
    }
}
