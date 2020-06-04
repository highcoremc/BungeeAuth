package org.nocraft.renay.bungee.auth;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungee.auth.config.AbstractConfiguration;
import org.nocraft.renay.bungee.auth.config.Configuration;
import org.nocraft.renay.bungee.auth.config.adapter.ConfigurationAdapter;
import org.nocraft.renay.bungee.auth.listener.ChatListener;
import org.nocraft.renay.bungee.auth.listener.LoginListener;
import org.nocraft.renay.bungee.auth.scheduler.Scheduler;
import org.nocraft.renay.bungee.auth.storage.Storage;
import org.nocraft.renay.bungee.auth.storage.StorageFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class BungeeAuth extends Plugin {

    private Configuration configuration;
    private Scheduler scheduler;
    private Storage storage;

    public void onEnable() {
        this.configuration = new AbstractConfiguration(this, provideConfigurationAdapter());

        StorageFactory storageFactory = new StorageFactory(this);
        this.storage = storageFactory.getInstance();

        this.scheduler = new Scheduler(this);

        this.getPluginManager().registerListener(this, new ChatListener(this));
        this.getPluginManager().registerListener(this, new LoginListener(this, storage));
    }

    public PluginManager getPluginManager() {
        return this.getProxy().getPluginManager();
    }

    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new BungeeConfigAdapter(this, resolveConfig());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File resolveConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            this.getDataFolder().mkdirs();
            try (InputStream is = this.getResourceAsStream("config.yml")) {
                Files.copy(is, configFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configFile;
    }

    public @NonNull Scheduler getScheduler() {
        return this.scheduler;
    }

    public void onDisable() {
        this.scheduler.shutdownScheduler();
    }

    public InputStream getResourceStream(String path) {
        return getResourceAsStream(path);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }
}
