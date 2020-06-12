package org.nocraft.renay.bungeeauth;

import com.github.games647.fastlogin.bungee.BungeeLoginSession;
import com.github.games647.fastlogin.bungee.FastLoginBungee;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.authentication.AttemptManager;
import org.nocraft.renay.bungeeauth.authentication.AuthFactory;
import org.nocraft.renay.bungeeauth.authentication.Authentication;
import org.nocraft.renay.bungeeauth.command.BungeeAuthCommand;
import org.nocraft.renay.bungeeauth.command.LoginCommand;
import org.nocraft.renay.bungeeauth.command.RegisterCommand;
import org.nocraft.renay.bungeeauth.config.AbstractConfiguration;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
import org.nocraft.renay.bungeeauth.config.Configuration;
import org.nocraft.renay.bungeeauth.config.adapter.ConfigurationAdapter;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethod;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodFactory;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodType;
import org.nocraft.renay.bungeeauth.listener.*;
import org.nocraft.renay.bungeeauth.scheduler.Scheduler;
import org.nocraft.renay.bungeeauth.storage.data.DataStorageFactory;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;
import org.nocraft.renay.bungeeauth.storage.entity.SimpleSessionStorage;
import org.nocraft.renay.bungeeauth.storage.entity.UserPassword;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.storage.session.SessionStorageFactory;
import org.nocraft.renay.bungeeauth.util.Composer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BungeeAuthPlugin extends Plugin {

    private final Map<UUID, BungeeAuthPlayer> authPlayers = new ConcurrentHashMap<>();

    private final Composer<BungeeAuthListener> listeners = new Composer<>();
    private final Composer<BungeeAuthCommand> commands = new Composer<>();

    private final Configuration configuration = new AbstractConfiguration(this, provideConfigurationAdapter());
    private AuthFactory authFactory;

    private SimpleSessionStorage sessionStorage;
    private SimpleDataStorage dataStorage;

    private AttemptManager attemptManager;
    private FastLoginBungee fastLogin;

    private Scheduler scheduler;

    private ServerManager serverManager;

    public void onEnable() {
        SessionStorageFactory sessionStorageFactory = new SessionStorageFactory(this);
        DataStorageFactory storageFactory = new DataStorageFactory(this);

        this.scheduler = new Scheduler(this);

        this.sessionStorage = sessionStorageFactory.getInstance();
        this.dataStorage = storageFactory.getInstance();
        Integer maxCountAttempts = this.getConfiguration()
                .get(ConfigKeys.MAX_AUTH_ATTEMPTS);
        this.attemptManager = new AttemptManager(this, maxCountAttempts);

        HashMethodType hashMethodType = this.getConfiguration()
                .get(ConfigKeys.HASH_METHOD_TYPE);
        int sessionTimeout = this.getConfiguration()
                .get(ConfigKeys.SESSION_TIMEOUT);
        this.authFactory = new AuthFactory(this, sessionTimeout, hashMethodType);

        Integer banTime = this.getConfiguration().get(ConfigKeys.BAN_TIME_MINUTES);

        this.serverManager = new ServerManager(this.getScheduler());

        List<String> loginServers = this.getConfiguration().get(ConfigKeys.LOGIN_SERVERS);
        List<String> gameServers = this.getConfiguration().get(ConfigKeys.GAME_SERVERS);

        setupConnectorServers(ServerType.LOGIN, loginServers, this.serverManager);
        setupConnectorServers(ServerType.GAME, gameServers, this.serverManager);

        AsyncLoginChecker task = new AsyncLoginChecker(this, loginServers);
        this.scheduler.asyncRepeating(task, 1, TimeUnit.SECONDS);

        this.listeners.add(new PlayerChatListener(this));
        this.listeners.add(new PlayerEnterListener(this));
        this.listeners.add(new PlayerLoginListener(this));
        this.listeners.add(new PlayerRegisterListener(this));
        this.listeners.add(new PlayerBanListener(this, banTime));
        this.listeners.register();

        this.commands.add(new RegisterCommand(this));
        this.commands.add(new LoginCommand(this));
        this.commands.register();

        setupHooks();
    }

    private void setupConnectorServers(ServerType type, List<String> servers, ServerManager connector) {
        servers.forEach(server -> connector.addServer(type, this.getProxy().getServerInfo(server)));
    }

    private void setupHooks() {
        Plugin fastLogin = getPluginManager().getPlugin("FastLogin");

        if (null == fastLogin) {
            getLogger().warning("Disable Premium GameProfile Authenticate because FastLogin is not found.");
            return;
        }

        this.fastLogin = (FastLoginBungee) fastLogin;
    }

    public ServerManager getServerManager() {
        return serverManager;
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
        this.listeners.unregister();
        this.commands.unregister();

        this.dataStorage.shutdown();
        this.sessionStorage.shutdown();
        this.scheduler.shutdownScheduler();
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

    public Authentication.Result authenticate(UUID uniqueId, String rawPassword) {
        BungeeAuthPlayer player = this.getAuthPlayers().get(uniqueId);

        if (null == player) {
            throw new IllegalStateException("Player not found in the list of connected players.");
        }

        if (!player.user.isRegistered() || !player.user.hasPassword()) {
            return Authentication.Result.ACCOUNT_NOT_FOUND;
        }

        if (null != player.session && player.session.isActive()) {
            return Authentication.Result.ALREADY_AUTHENTICATED;
        }

        UserPassword password = player.user.getPassword();
        HashMethod hm = HashMethodFactory
                .create(password.hashMethodType);

        if (!password.verify(hm, rawPassword)) {
            return Authentication.Result.WRONG_PASSWORD;
        }

        return Authentication.Result.SUCCESS_LOGIN;
    }

    public CompletableFuture<Void> updateAuthSession(BungeeAuthPlayer player) throws IllegalStateException {
        Optional<Session> session = this.authFactory.createSession(player.user.uniqueId);

        // it will be thrown when the player was disconnected after the login command
        if (!session.isPresent()) {
            throw new IllegalStateException("Unable to create user session.");
        }

        player.changeActiveSession(session.get());
        return this.sessionStorage.save(session.get());
    }

    public Map<UUID, BungeeAuthPlayer> getAuthPlayers() {
        return this.authPlayers;
    }

    public SimpleDataStorage getDataStorage() {
        return this.dataStorage;
    }

    public SimpleSessionStorage getSessionStorage() {
        return this.sessionStorage;
    }

    public Optional<ProxiedPlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(this.getProxy().getPlayer(uniqueId));
    }

    public AttemptManager getAttemptManager() {
        return this.attemptManager;
    }

    public AuthFactory getAuthFactory() {
        return this.authFactory;
    }

    public boolean isAuthenticated(UUID uniqueId) {
        BungeeAuthPlayer player = this.authPlayers.get(uniqueId);

        if (null == player) {
            return false;
        }

        return player.isAuthenticated();
    }
}
