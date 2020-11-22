package org.nocraft.renay.bungeeauth.config.adapter;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConfigurationAdapter {

    BungeeAuthPlugin getPlugin();

    void reload();

    String getString(String path, String def);

    int getInteger(String path, int def);

    boolean getBoolean(String path, boolean def);

    List<String> getStringList(String path, List<String> def);

    List<String> getKeys(String path, List<String> def);

    Map<String, String> getStringMap(String path, Map<String, String> def);

    Map<String, List<String>> getListString(String path, Map<String, List<String>> def);
}
