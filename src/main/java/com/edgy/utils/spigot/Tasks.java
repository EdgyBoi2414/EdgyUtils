package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Tasks {

  private static List<BukkitTask> asyncTasks = new ArrayList<>();

  public static void clearAsyncs() {
    for (BukkitTask task : asyncTasks) {
      task.cancel();
    }
  }

  public static BukkitTask run(Runnable runnable) {
    return runLater(runnable, 0L);
  }
  public static BukkitTask runLater(Runnable runnable, long delay) {
    return Bukkit.getScheduler().runTaskLater(EdgyUtils.bukkit(), runnable, delay);
  }

  public static BukkitTask runTimer(Runnable runnable, long delay, long period) {
    return Bukkit.getScheduler().runTaskTimer(EdgyUtils.bukkit(), runnable, delay, period);
  }

  public static void forceRunSync(final Runnable task) {
    if (!Bukkit.isPrimaryThread()) {
      final CountDownLatch latch = new CountDownLatch(1);
        (new BukkitRunnable() {
          public void run() {
            task.run();
            latch.countDown();
          }
        }).runTask(EdgyUtils.bukkit());
      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      task.run();
    }
  }

  public static BukkitTask runAsync(Runnable runnable) {
    BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(EdgyUtils.bukkit(), runnable);
    asyncTasks.add(task);
    return task;
  }

  public static BukkitTask runTimerAsync(Runnable runnable, long delay, long period) {
    BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(EdgyUtils.bukkit(), runnable, delay, period);
    asyncTasks.add(task);
    return task;
  }
}