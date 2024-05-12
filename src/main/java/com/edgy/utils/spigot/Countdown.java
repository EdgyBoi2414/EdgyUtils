package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.MapBuilder;
import com.edgy.utils.shared.Messages;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown {

  public static void of(
      int seconds,
      List<Player> players,
      String formatTitle,
      Runnable runnable
  ) {
    Messages<CommandSender> messages = EdgyUtils.bukkit().messages();
    BukkitAudiences bukkitAudiences = BukkitAudiences.create(EdgyUtils.bukkit());
    Audience audience = Audience.audience(
        players.stream()
            .map(bukkitAudiences::player)
            .toArray(Audience[]::new)
    );

    new BukkitRunnable() {
      int time = seconds;

      @Override
      public void run() {
        if (time == 0) {
          runnable.run();
          cancel();
        } else {
          audience.showTitle(
              Title.title(
                  messages.component(formatTitle,
                      MapBuilder.mapOf(
                          "time", time
                      )
                  ),
                  Component.empty()
              )
          );
          time--;
        }
      }
    }.runTaskTimerAsynchronously(EdgyUtils.bukkit(), 0, 20);
  }

}
