package org.nocraft.renay.bungeeauth.config;

import org.nocraft.renay.bungeeauth.util.ImmutableCollectors;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.nocraft.renay.bungeeauth.config.ConfigKeyTypes.customKey;
import static org.nocraft.renay.bungeeauth.config.ConfigKeyTypes.enduringKey;

public class MessageKeys {

    private MessageKeys() {}

    public static final ConfigKey<Message> FAIL_SESSION_CREATION = enduringKey(customKey(
            c -> new Message(c.getString("fail-session-creation", ""))));

    public static final ConfigKey<Message> FAILED_AUTHENTICATION = enduringKey(customKey(
            c -> new Message(c.getString("failed-authentication", ""))));

    public static final ConfigKey<Message> LOGIN_TIMEOUT = enduringKey(customKey(
            c -> new Message(c.getString("timeout-login", ""))));

    public static final ConfigKey<Message> FAILED_REGISTER = enduringKey(customKey(
            c -> new Message(c.getString("forbidden-access", ""))));

    public static final ConfigKey<Message> EXCEEDED_LOGIN_ATTEMPTS = enduringKey(customKey(
            c -> new Message(c.getString("exceeded-login-attempts", ""))));

    public static final ConfigKey<Message> BAD_NICKNAME = enduringKey(customKey(
            c -> new Message(c.getString("bad-nickname", ""))));

    public static final ConfigKey<Message> BAD_REQUEST = enduringKey(customKey(
            c -> new Message(c.getString("bad-request", ""))));

    public static final ConfigKey<Message> WRONG_PASSWORD = enduringKey(customKey(
            c -> new Message(c.getString("wrong-password", ""))));

    public static final ConfigKey<Message> PASSWORD_MISMATCH = enduringKey(customKey(
            c -> new Message(c.getString("password-mismatch", ""))));

    public static final ConfigKey<Message> PASSWORD_MIN_LENGTH = enduringKey(customKey(
            c -> new Message(c.getString("password-min-length", ""))));

    public static final ConfigKey<Message> ACCOUNT_NOT_FOUND = enduringKey(customKey(
            c -> new Message(c.getString("account-not-found", ""))));

    public static final ConfigKey<Message> USER_AUTHENTICATED = enduringKey(customKey(
            c -> new Message(c.getString("user-authenticated", ""))));

    public static final ConfigKey<Message> LOGIN_USAGE = enduringKey(customKey(
            c -> new Message(c.getString("login-usage", ""))));

    public static final ConfigKey<Message> LOGIN_TITLE = enduringKey(customKey(
            c -> new Message(c.getString("login-title", ""))));

    public static final ConfigKey<Message> LOGIN_SUBTITLE = enduringKey(customKey(
            c -> new Message(c.getString("login-subtitle", ""))));

    public static final ConfigKey<Message> LOGIN_CHAT_MESSAGE = enduringKey(customKey(
            c -> new Message(c.getString("login-text", ""))));

    public static final ConfigKey<Message> REGISTER_TITLE = enduringKey(customKey(
            c -> new Message(c.getString("register-title", ""))));

    public static final ConfigKey<Message> REGISTER_SUBTITLE = enduringKey(customKey(
            c -> new Message(c.getString("register-subtitle", ""))));

    public static final ConfigKey<Message> REGISTER_CHAT_MESSAGE = enduringKey(customKey(
            c -> new Message(c.getString("register-text", ""))));

    public static final ConfigKey<Message> ALREADY_REGISTERED = enduringKey(customKey(
            c -> new Message(c.getString("already-registered", ""))));

    public static final ConfigKey<Message> NO_ACTUAL_SERVER = enduringKey(customKey(
            c -> new Message(c.getString("no-actual-server", ""))));

    public static final ConfigKey<Message> VERSION_OUTDATED = enduringKey(customKey(
            c -> new Message(c.getString("version-outdated", ""))));


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
