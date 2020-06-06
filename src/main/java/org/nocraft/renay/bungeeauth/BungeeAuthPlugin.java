package org.nocraft.renay.bungeeauth;

import com.github.games647.fastlogin.bungee.BungeeLoginSession;
import com.github.games647.fastlogin.bungee.FastLoginBungee;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.config.AbstractConfiguration;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
import org.nocraft.renay.bungeeauth.config.Configuration;
import org.nocraft.renay.bungeeauth.config.adapter.ConfigurationAdapter;
import org.nocraft.renay.bungeeauth.listener.ChatListener;
import org.nocraft.renay.bungeeauth.listener.LoginListener;
import org.nocraft.renay.bungeeauth.scheduler.Scheduler;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;
import org.nocraft.renay.bungeeauth.storage.data.DataStorageFactory;
import org.nocraft.renay.bungeeauth.storage.session.SessionStorageFactory;
import org.nocraft.renay.bungeeauth.storage.entity.SimpleSessionStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class BungeeAuthPlugin extends Plugin {

    private SimpleSessionStorage sessionStorage;
    private SimpleDataStorage dataStorage;
    private SessionFactory sessionFactory;
    private Configuration configuration;
    private FastLoginBungee fastLogin;
    private Scheduler scheduler;

    public void onEnable() {
        this.configuration = new AbstractConfiguration(this, provideConfigurationAdapter());

        DataStorageFactory storageFactory = new DataStorageFactory(this);
        this.dataStorage = storageFactory.getInstance();

        SessionStorageFactory sessionStorageFactory = new SessionStorageFactory(this);
        this.sessionStorage = sessionStorageFactory.getInstance();

        this.sessionFactory = new SessionFactory(this.getConfiguration().get(ConfigKeys.SESSION_TIMEOUT));

        this.scheduler = new Scheduler(this);

        this.getPluginManager().registerListener(this, new ChatListener(this));
        this.getPluginManager().registerListener(this, new LoginListener(this, dataStorage, sessionStorage));

        setupHooks();
    }

    private void setupHooks() {
        Plugin fastLogin = getPluginManager().getPlugin("FastLogin");

        if (null == fastLogin) {
            getLogger().warning("Disable Premium GameProfile Authenticate because FastLogin is not found.");
            return;
        }

        this.fastLogin = (FastLoginBungee) fastLogin;
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
        this.dataStorage.shutdown();
    }

    public InputStream getResourceStream(String path) {
        return getResourceAsStream(path);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public boolean isPremiumProfile(PendingConnection connection) {
        BungeeLoginSession session = this.fastLogin.getSession(connection);

        if (session == null) {
            getLogger().warning(String.format(
                    "Failed to get information about player %s:%s because FastLogin does not have this connection",
                    connection.getName(), connection.getUniqueId()));
            return false;
        }

        return session.getProfile().isPremium();
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }
}
