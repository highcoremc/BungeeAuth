package me.loper.bungeeauth.config;

import me.loper.configuration.ConfigKey;

import static me.loper.bungeeauth.config.ConfigKeyTypes.messageKey;

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

    public static final ConfigKey<Message> ACCOUNT_NOT_REGISTERED = messageKey(
            c -> c.getString("account-not-registered", ""));

    public static final ConfigKey<Message> USER_AUTHENTICATED = messageKey(
            c -> c.getString("user-authenticated", ""));

    public static final ConfigKey<Message> LOGIN_USAGE = messageKey(
            c -> c.getString("usage-account-login", ""));

    public static final ConfigKey<Message> LOGIN_TITLE = messageKey(
            c -> c.getString("login-title", ""));

    public static final ConfigKey<Message> LOGIN_SUBTITLE = messageKey(
            c -> c.getString("login-subtitle", ""));

    public static final ConfigKey<Message> LOGIN_CHAT_MESSAGE = messageKey(
            c -> c.getString("tip-account-login", ""));

    public static final ConfigKey<Message> REGISTER_TITLE = messageKey(
            c -> c.getString("register-title", ""));

    public static final ConfigKey<Message> REGISTER_SUBTITLE = messageKey(
            c -> c.getString("register-subtitle", ""));

    public static final ConfigKey<Message> REGISTER_CHAT_MESSAGE = messageKey(
            c -> c.getString("tip-account-register", ""));

    public static final ConfigKey<Message> ALREADY_REGISTERED = messageKey(
            c -> c.getString("already-registered", ""));

    public static final ConfigKey<Message> NO_ACTUAL_SERVER = messageKey(
            c -> c.getString("no-actual-server", ""));

    public static final ConfigKey<Message> VERSION_OUTDATED = messageKey(
            c -> c.getString("version-outdated", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_SELF_USAGE = messageKey(
        c -> c.getString("changepassword-usage-self", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_OTHER_USAGE = messageKey(
        c -> c.getString("changepassword-usage-other", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_OTHER_SUCCESS = messageKey(
        c -> c.getString("changepassword-success-other", ""));

    public static final ConfigKey<Message> CHANGEPASSWORD_SELF_SUCCESS = messageKey(
        c -> c.getString("changepassword-success-self", ""));

}
