package org.nocraft.renay.bungeeauth.config;

import org.nocraft.renay.bungeeauth.config.adapter.ConfigurationAdapter;

import java.util.function.Function;

public class ConfigKeyTypes {

    public static <T> CustomKey<T> customKey(Function<ConfigurationAdapter, T> function) {
        return new CustomKey<>(function);
    }

    public static <T> EnduringKey<T> enduringKey(ConfigKey<T> delegate) {
        return new EnduringKey<>(delegate);
    }

    public abstract static class BaseConfigKey<T> implements ConfigKey<T> {
        int ordinal = -1;

        BaseConfigKey() {

        }

        @Override
        public int ordinal() {
            return this.ordinal;
        }
    }

    public static class CustomKey<T> extends BaseConfigKey<T> {
        private final Function<ConfigurationAdapter, T> function;

        private CustomKey(Function<ConfigurationAdapter, T> function) {
            this.function = function;
        }

        @Override
        public T get(ConfigurationAdapter adapter) {
            return this.function.apply(adapter);
        }
    }

    public static class EnduringKey<T> extends BaseConfigKey<T> {
        private final ConfigKey<T> delegate;

        private EnduringKey(ConfigKey<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get(ConfigurationAdapter adapter) {
            return this.delegate.get(adapter);
        }
    }

}
