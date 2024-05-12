package com.edgy.utils.bungee;

import com.edgy.utils.EdgyUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeeTasks {

  private static List<ScheduledTask> asyncTasks = new ArrayList<>();

  public static void clearAsyncs() {
    for (ScheduledTask task : asyncTasks) {
      task.cancel();
    }
  }

  public static ScheduledTask run(Runnable runnable) {
    return runLater(runnable, 0L, TimeUnit.MILLISECONDS);
  }

  public static ScheduledTask runLater(Runnable runnable, long delay, TimeUnit unit) {
    return ProxyServer.getInstance().getScheduler().schedule(EdgyUtils.bungee(), runnable, delay, unit);
  }

  public static ScheduledTask runTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
    return ProxyServer.getInstance().getScheduler().schedule(EdgyUtils.bungee(), runnable, delay, period, unit);
  }

  public static ScheduledTask runAsync(Runnable runnable) {
    ScheduledTask task = ProxyServer.getInstance().getScheduler().runAsync(EdgyUtils.bungee(), runnable);
    asyncTasks.add(task);
    return task;
  }

  public static ScheduledTask runTimerAsync(Runnable runnable, long delay, long period, TimeUnit unit) {
    ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(EdgyUtils.bungee(), runnable, delay, period, unit);
    asyncTasks.add(task);
    return task;
  }

}
