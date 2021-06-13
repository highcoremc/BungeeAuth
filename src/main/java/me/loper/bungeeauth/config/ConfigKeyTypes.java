package me.loper.bungeeauth.config;

import me.loper.configuration.adapter.ConfigurationAdapter;

import java.util.function.Function;

public class ConfigKeyTypes {
    public ConfigKeyTypes() {
    }

    public static class MessageKey extends me.loper.configuration.ConfigKeyTypes.BaseConfigKey<Message> {
        private final Function<ConfigurationAdapter, String> function;

        private MessageKey(Function<ConfigurationAdapter, String> function) {
            this.function = function;
        }

        public Message get(ConfigurationAdapter adapter) {
            return new Message(this.function.apply(adapter));
        }
    }

    public static MessageKey messageKey(Function<ConfigurationAdapter, String> function) {
        return new MessageKey(function);
    }
}
