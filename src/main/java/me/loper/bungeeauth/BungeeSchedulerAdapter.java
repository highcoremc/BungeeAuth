package me.loper.bungeeauth;

import me.loper.scheduler.SchedulerAdapter;
import me.loper.scheduler.SchedulerTask;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import me.loper.bungeeauth.util.Iterators;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class BungeeSchedulerAdapter implements SchedulerAdapter {

    private final BungeeAuthPlugin plugin;

    private final Executor executor;
    private final Set<ScheduledTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());

    public BungeeSchedulerAdapter(BungeeAuthPlugin plugin) {
        this.plugin = plugin;
        this.executor = r -> plugin.getProxy().getScheduler().runAsync(plugin, r);
    }

    public Executor async() {
        return this.executor;
    }

    @Override
    public Executor sync() {
        return this.executor;
    }

    @Override
    public SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask t = this.plugin.getProxy().getScheduler().schedule(this.plugin, task, delay, unit);
        this.tasks.add(t);
        return t::cancel;
    }

    @Override
    public SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledTask t = this.plugin.getProxy().getScheduler().schedule(this.plugin, task, interval, interval, unit);
        this.tasks.add(t);
        return t::cancel;
    }

    @Override
    public SchedulerTask syncRepeating(Runnable runnable, long l, TimeUnit timeUnit) {
        return null;
    }

    public void shutdownScheduler() {
        Iterators.tryIterate(this.tasks, ScheduledTask::cancel);
    }

    @Override
    public void shutdownExecutor() {
    }
}