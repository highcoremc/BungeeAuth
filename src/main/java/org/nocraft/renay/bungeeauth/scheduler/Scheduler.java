package org.nocraft.renay.bungeeauth.scheduler;

import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.util.Iterators;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class Scheduler  {
    private final BungeeAuthPlugin plugin;

    private final Executor executor;
    private final Set<ScheduledTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());

    public Scheduler(BungeeAuthPlugin plugin) {
        this.plugin = plugin;
        this.executor = r -> plugin.getProxy().getScheduler().runAsync(plugin, r);
    }

    public Executor async() {
        return this.executor;
    }

    public SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask t = this.plugin.getProxy().getScheduler().schedule(this.plugin, task, delay, unit);
        this.tasks.add(t);
        return t::cancel;
    }

    public SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledTask t = this.plugin.getProxy().getScheduler().schedule(this.plugin, task, interval, interval, unit);
        this.tasks.add(t);
        return t::cancel;
    }

    public void shutdownScheduler() {
        Iterators.tryIterate(this.tasks, ScheduledTask::cancel);
    }
}