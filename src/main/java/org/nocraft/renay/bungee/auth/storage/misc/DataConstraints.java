package org.nocraft.renay.bungee.auth.storage.misc;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DataConstraints {
    private DataConstraints() {}

    public static final int MAX_PERMISSION_LENGTH = 200;

    public static final int MAX_TRACK_NAME_LENGTH = 36;
    public static final int MAX_GROUP_NAME_LENGTH = 36;

    public static final int MAX_PLAYER_USERNAME_LENGTH = 16;
    public static final Pattern PLAYER_USERNAME_INVALID_CHAR_MATCHER = Pattern.compile("[^A-Za-z0-9_]");

    public static final int MAX_SERVER_LENGTH = 36;
    public static final int MAX_WORLD_LENGTH = 36;

    public static final Predicate<String> PERMISSION_TEST = s -> !s.isEmpty() && s.length() <= MAX_PERMISSION_LENGTH;

    public static final Predicate<String> PLAYER_USERNAME_TEST = s -> !s.isEmpty() && s.length() <= MAX_PLAYER_USERNAME_LENGTH && !PLAYER_USERNAME_INVALID_CHAR_MATCHER.matcher(s).find();

    public static final Predicate<String> PLAYER_USERNAME_TEST_LENIENT = s -> !s.isEmpty() && s.length() <= MAX_PLAYER_USERNAME_LENGTH;

    public static final Predicate<String> GROUP_NAME_TEST = s -> !s.isEmpty() && s.length() <= MAX_GROUP_NAME_LENGTH && !s.contains(" ");

    public static final Predicate<String> GROUP_NAME_TEST_ALLOW_SPACE = s -> !s.isEmpty() && s.length() <= MAX_GROUP_NAME_LENGTH;

    public static final Predicate<String> TRACK_NAME_TEST = s -> !s.isEmpty() && s.length() <= MAX_TRACK_NAME_LENGTH && !s.contains(" ");

    public static final Predicate<String> TRACK_NAME_TEST_ALLOW_SPACE = s -> !s.isEmpty() && s.length() <= MAX_TRACK_NAME_LENGTH;

    public static final Predicate<String> SERVER_NAME_TEST = s -> !s.isEmpty() && s.length() <= MAX_SERVER_LENGTH && !s.contains(" ");

    public static final Predicate<String> WORLD_NAME_TEST = s -> !s.isEmpty() && s.length() <= MAX_WORLD_LENGTH;

}
