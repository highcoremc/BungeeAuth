package me.loper.bungeeauth;

import me.loper.configuration.adapter.ConfigurationAdapter;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BungeeMessageAdapter implements ConfigurationAdapter {
    private final BungeeAuthPlugin plugin;
    private Configuration configuration;
    private final File file;

    public BungeeMessageAdapter(BungeeAuthPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        reload();
    }

    public BungeeAuthPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void reload() {
        try {
            this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getString(String path, String def) {
        return this.configuration.getString(path, def);
    }

    @Override
    public int getInteger(String path, int def) {
        return this.configuration.getInt(path, def);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return this.configuration.getBoolean(path, def);
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return Optional.of(this.configuration.getStringList(path)).orElse(def);
    }

    @Override
    public List<String> getKeys(String path, List<String> def) {
        Configuration section = this.configuration.getSection(path);

        if (section == null) {
            return def;
        }

        return Optional.of((List<String>) new ArrayList<>(section.getKeys())).orElse(def);
    }

    @Override
    public Map<String, String> getStringMap(String path, Map<String, String> def) {
        Map<String, String> map = new HashMap<>();
        Configuration section = this.configuration.getSection(path);

        if (section == null) {
            return def;
        }

        for (String key : section.getKeys()) {
            map.put(key, section.get(key).toString());
        }

        return map;
    }

    @Override
    public Map<String, List<String>> getListString(String path, Map<String, List<String>> def) {
        Configuration section = this.configuration.getSection(path);

        Map<String, List<String>> map = new HashMap<>();

        if (section == null) {
            return def;
        }

        for (String key : section.getKeys()) {
            map.put(key, section.getStringList(key));
        }

        return map;
    }
}
