package org.nocraft.renay.bungeeauth.hash;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Sha256HashMethod implements HashMethod {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public String createHash(String entry) {
        return Hashing.sha256().newHasher().putString(entry, Charsets.UTF_8).hash().toString();
    }

    @Override
    public boolean verify(@NonNull String entry, @NonNull String actual) {
        return createHash(entry).equals(actual);
    }
}
