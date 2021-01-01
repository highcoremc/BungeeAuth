package org.nocraft.renay.bungeeauth.config;

import org.nocraft.renay.bungeeauth.util.ImmutableCollectors;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.nocraft.renay.bungeeauth.config.ConfigKeyTypes.*;

public class MessageKeys {

    private MessageKeys() {}

    public static final ConfigKey<Message> FAIL_SESSION_CREATION = messageKey(
            c -> c.getString("fail-session-creation", ""));

    public static final ConfigKey<Message> FAILED_AUTHENTICATION = messageKey(
            c -> c.getString("failed-authentication", ""));

    public static final ConfigKey<Message> LOGIN_TIMEOUT = messageKey(
            c -> c.getString("timeout-login", ""));

    public static final ConfigKey<Message> FAILED_REGISTER = messageKey(
            c -> c.getString("failed-register", ""));

    public static final ConfigKey<Message> TEMPORARY_FORBIDDEN_ACCESS = messageKey(
            c -> c.getString("temporary-forbidden-access", ""));

    public static final ConfigKey<Message> FORBIDDEN_ACCESS = messageKey(
            c -> c.getString("forbidden-access", ""));

    public static final ConfigKey<Message> EXCEEDED_LOGIN_ATTEMPTS = messageKey(
            c -> c.getString("exceeded-login-attempts", ""));

    public static final ConfigKey<Message> BAD_NICKNAME = messageKey(
            c -> c.getString("bad-nickname", ""));

    public static final ConfigKey<Message> BAD_REQUEST = messageKey(
            c -> c.getString("bad-request", ""));

    public static final ConfigKey<Message> WRONG_PASSWORD = messageKey(
            c -> c.getString("wrong-password", ""));

    public static final ConfigKey<Message> PASSWORD_MISMATCH = messageKey(
            c -> c.getString("password-mismatch", ""));

    public static final ConfigKey<Message> PASSWORD_MIN_LENGTH = messageKey(
            c -> c.getString("password-min-length", ""));

    public static final ConfigKey<Message> AUTHENTICATION_ACCOUNT_NOT_FOUND = messageKey(
            c -> c.getString("authentication-account-not-found", ""));

    public static final ConfigKey<Message> PLAYER_ACCOUNT_NOT_FOUND = messageKey(
            c -> c.getString("player-account-not-found", ""));

    public static final ConfigKey<Message> USER_AUTHENTICATED = messageKey(
            c -> c.getString("user-authenticated", ""));

    public static final ConfigKey<Message> LOGIN_USAGE = messageKey(
            c -> c.getString("login-usage", ""));

    public static final ConfigKey<Message> LOGIN_TITLE = messageKey(
            c -> c.getString("login-title", ""));

    public static final ConfigKey<Message> LOGIN_SUBTITLE = messageKey(
            c -> c.getString("login-subtitle", ""));

    public static final ConfigKey<Message> LOGIN_CHAT_MESSAGE = messageKey(
            c -> c.getString("login-text", ""));

    public static final ConfigKey<Message> REGISTER_TITLE = messageKey(
            c -> c.getString("register-title", ""));

    public static final ConfigKey<Message> REGISTER_SUBTITLE = messageKey(
            c -> c.getString("register-subtitle", ""));

    public static final ConfigKey<Message> REGISTER_CHAT_MESSAGE = messageKey(
            c -> c.getString("register-text", ""));

    public static final ConfigKey<Message> ALREADY_REGISTERED = messageKey(
            c -> c.getString("already-registered", ""));

    public static final ConfigKey<Message> NO_ACTUAL_SERVER = messageKey(
            c -> c.getString("no-actual-server", ""));

    public static final ConfigKey<Message> VERSION_OUTDATED = messageKey(
            c -> c.getString("version-outdated", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_PLAYER_USAGE = messageKey(
        c -> c.getString("change-player-password-usage", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_CONSOLE_USAGE = messageKey(
        c -> c.getString("change-console-password-usage", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_WRONG_CONFIRM = messageKey(
        c -> c.getString("changepassword-wrong-confirm-password", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_OTHER_SUCCESS = messageKey(
        c -> c.getString("changepassword-other-success", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_SELF_SUCCESS = messageKey(
        c -> c.getString("changepassword-self-success", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_REAUTHENTICATE = messageKey(
        c -> c.getString("changepassword-reauthenticate", ""));


    private static final List<ConfigKeyTypes.BaseConfigKey<?>> KEYS;

    static {
        // get a list of all keys
        KEYS = Arrays.stream(MessageKeys.class.getFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> ConfigKey.class.equals(f.getType()))
                .map(f -> {
                    try {
                        return (ConfigKeyTypes.BaseConfigKey<?>) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(ImmutableCollectors.toList());

        // set ordinal values
        for (int i = 0; i < KEYS.size(); i++) {
            KEYS.get(i).ordinal = i;
        }
    }

    /**
     * Gets a list of the keys defined in this class.
     *
     * @return the defined keys
     */
    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }
}
