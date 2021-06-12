package me.loper.bungeeauth.authentication.hash;

public class HashMethodFactory {

    public static HashMethod create(HashMethodType type) {
        switch (type) {
            case BCRYPT:
                return new BcryptHashMethod();
            case SHA256:
                return new Sha256HashMethod();
            default:
                throw new IllegalStateException("Undefined type of hash method.");
        }
    }
}
