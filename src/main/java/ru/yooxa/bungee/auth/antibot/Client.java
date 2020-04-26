package ru.yooxa.bungee.auth.antibot;

import java.util.HashSet;
import java.util.Set;

public class Client {
    private String ip;
    private long time;
    private Set<String> names;

    public Client(String name, String ip)
    {
        this.ip = ip;
        this.time = System.currentTimeMillis() / 1000L;
        this.names = new HashSet();
        this.names.add(name);
    }

    public Set<String> getNames() { return this.names; }

    public String getIp() { return this.ip; }

    public long getTime() { return this.time; }
}


