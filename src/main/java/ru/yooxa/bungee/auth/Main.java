package ru.yooxa.bungee.auth;

import net.md_5.bungee.api.plugin.Plugin;
import ru.yooxa.bungee.auth.hash.HashAlgorithm;

public class Main extends Plugin
{
    private static Main instance;
    public AuthManager manager;

    public static HashAlgorithm getAlgorithm()
    {
        return HashAlgorithm.valueOf(AuthManager.getInstance().getConfig().getString("hash"));
    }

    public static boolean isMail()
    {
        return AuthManager.getInstance().getConfig().getBoolean("mail");
    }

    public static Main getInstance()
    {
        return instance;
    }

    public void onEnable()
    {
        instance = this;
        this.manager = new AuthManager(this);
    }
}


