package me.loper.bungeeauth;

import me.loper.bungeeauth.authentication.AttemptCalculator;
import me.loper.bungeeauth.authentication.AuthFactory;
import me.loper.bungeeauth.authentication.Authentication;
import me.loper.bungeeauth.authentication.hash.HashMethod;
import me.loper.bungeeauth.authentication.hash.HashMethodFactory;
import me.loper.bungeeauth.authentication.hash.HashMethodType;
import me.loper.bungeeauth.command.BungeeAuthCommand;
import me.loper.bungeeauth.command.ChangePasswordCommand;
import me.loper.bungeeauth.command.LoginCommand;
import me.loper.bungeeauth.command.RegisterCommand;
import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.config.Configuration;
import me.loper.bungeeauth.config.DefaultConfiguration;
import me.loper.bungeeauth.config.MessageConfiguration;
import me.loper.bungeeauth.config.adapter.ConfigurationAdapter;
import me.loper.bungeeauth.listener.*;
import me.loper.bungeeauth.scheduler.Scheduler;
import me.loper.bungeeauth.server.Server;
import me.loper.bungeeauth.server.ServerManager;
import me.loper.bungeeauth.server.ServerType;
import me.loper.bungeeauth.service.AuthManager;
import me.loper.bungeeauth.storage.data.DataStorageFactory;
import me.loper.bungeeauth.storage.data.SimpleDataStorage;
import me.loper.bungeeauth.storage.entity.SimpleSessionStorage;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.UserPassword;
import me.loper.bungeeauth.storage.session.Session;
import me.loper.bungeeauth.storage.session.SessionStorageFactory;
import me.loper.bungeeauth.util.Composer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BungeeAuthPlugin extends Plugin {

    private final Map<UUID, BungeeAuthPlayer> authPlayers = new ConcurrentHashMap<>();

    private final Composer<BungeeAuthListener> listeners = new Composer<>();
    private final Composer<BungeeAuthCommand> commands = new Composer<>();

    private AuthFactory authFactory;

    private final Configuration configuration = new DefaultConfiguration(this, provideConfigurationAdapter());
    private final Configuration messages = new MessageConfiguration(this, provideMessageConfigurationAdapter());

    private SimpleSessionStorage sessionStorage;
    private SimpleDataStorage dataStorage;

    private AttemptCalculator attemptCalculator;

    private ServerManager serverManager;
    private AuthManager authManager;
    private Scheduler scheduler;

    public void onEnable() {
        SessionStorageFactory sessionStorageFactory = new SessionStorageFactory(this);
        DataStorageFactory storageFactory = new DataStorageFactory(this);

        this.scheduler = new Scheduler(this);

        this.sessionStorage = sessionStorageFactory.getInstance();
        this.dataStorage = storageFactory.getInstance();

        if (!this.sessionStorage.isLoaded() || !this.dataStorage.isLoaded()) {
            this.getProxy().stop("BungeeAuth can not start up.");
            return;
        }

        Integer maxCountAttempts = this.getConfiguration()
                .get(ConfigKeys.MAX_AUTH_ATTEMPTS);
        this.attemptCalculator = new AttemptCalculator(this, maxCountAttempts);

        HashMethodType hashMethodType = this.getConfiguration()
                .get(ConfigKeys.HASH_METHOD_TYPE);
        int sessionTimeout = this.getConfiguration()
                .get(ConfigKeys.SESSION_TIMEOUT);
        this.authFactory = new AuthFactory(this, sessionTimeout, hashMethodType);

        Integer banTime = this.getConfiguration().get(ConfigKeys.BAN_TIME_MINUTES);

        this.serverManager = new ServerManager(this, this.getScheduler());
        this.authManager = new AuthManager(this);

        this.scheduler.async().execute(() -> this.authManager.authenticateOnlinePlayers());

        List<String> loginServers = this.getConfiguration().get(ConfigKeys.LOGIN_SERVERS);
        List<String> gameServers = this.getConfiguration().get(ConfigKeys.GAME_SERVERS);

        setupConnectorServers(ServerType.LOGIN, loginServers, this.serverManager);
        setupConnectorServers(ServerType.GAME, gameServers, this.serverManager);

        AsyncLoginChecker task = new AsyncLoginChecker(this, loginServers);
        this.scheduler.asyncRepeating(task, 1, TimeUnit.SECONDS);

        this.listeners.add(new PlayerChatListener(this));
        this.listeners.add(new PlayerEnterListener(this));
        this.listeners.add(new PlayerLoginListener(this));
        this.listeners.add(new ChangePasswordListener(this));
        this.listeners.add(new PlayerRegisterListener(this));
        this.listeners.add(new PlayerBanListener(this, banTime));
        this.listeners.register();

        this.commands.add(new ChangePasswordCommand(this));
        this.commands.add(new RegisterCommand(this));
        this.commands.add(new LoginCommand(this));
        this.commands.register();
    }

    private void setupConnectorServers(ServerType type, List<String> servers, ServerManager connector) {
        servers.forEach(server -> {
            ServerInfo info = this.getProxy().getServerInfo(server);

            if (null == info) {
                return;
            }

            connector.addServer(type, Server.wrap(info));
        });
    }


    public ServerManager getServerManager() {
        return serverManager;
    }

    public PluginManager getPluginManager() {
        return this.getProxy().getPluginManager();
    }

    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new BungeeConfigAdapter(this, resolveConfig("config.yml"));
    }

    private ConfigurationAdapter provideMessageConfigurationAdapter() {
        return new BungeeMessageAdapter(this, resolveConfig("messages.yml"));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File resolveConfig(String fileName) {
        File configFile = new File(this.getDataFolder(), fileName);

        if (!configFile.exists()) {
            this.getDataFolder().mkdirs();
            try (InputStream is = this.getResourceAsStream(fileName)) {
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

    public Configuration getMessageConfig() {
        return this.messages;
    }

    public Authentication.Result authenticate(UUID uniqueId, String rawPassword) {
        BungeeAuthPlayer player = this.getAuthPlayer(uniqueId);

        if (null == player) {
            throw new IllegalStateException("Player not found in the list of connected players.");
        }

        if (!player.user.isRegistered() || !player.user.hasPassword()) {
            return Authentication.Result.AUTHENTICATION_FAILED;
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

    public @Nullable BungeeAuthPlayer getAuthPlayer(UUID uniqueId) {
        return this.authPlayers.get(uniqueId);
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

    public AttemptCalculator getAttemptCalculator() {
        return this.attemptCalculator;
    }

    public AuthFactory getAuthFactory() {
        return this.authFactory;
    }

    public boolean isAuthenticated(ProxiedPlayer p) {
        return isAuthenticated(p.getUniqueId());
    }

    public boolean isAuthenticated(UUID uniqueId) {
        BungeeAuthPlayer player = this.authPlayers.get(uniqueId);

        if (null == player) {
            return false;
        }

        return player.isAuthenticated();
    }

    public AuthManager getAuthManager() {
        return this.authManager;
    }

    public void addAuthPlayer(BungeeAuthPlayer player) {
        this.authPlayers.put(player.user.uniqueId, player);
    }

    public boolean hasAuthPlayer(UUID uniqueId) {
        return this.authPlayers.containsKey(uniqueId);
    }

    public void removeAuthPlayer(UUID uniqueId) {
        this.authPlayers.remove(uniqueId);
    }

    public void dropSessions(List<Session> sessions) {
        this.sessionStorage.remove(sessions);
    }

    public CompletableFuture<Optional<User>> loadUser(String playerName) {
        return this.dataStorage.loadUser(playerName);
    }
}
