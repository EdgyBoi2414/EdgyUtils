package com.edgy.utils.spigot;

import com.edgy.utils.spigot.fastboard.FastBoard;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

public class Scoreboards {

  private static final HashMap<UUID, FastBoard> boards = new HashMap<>();

  public static FastBoard get(Player player) {
    if (!boards.containsKey(player.getUniqueId())) {
      boards.put(player.getUniqueId(), new FastBoard(player));
    }

    return boards.get(player.getUniqueId());
  }

  public static void remove(Player player) {
    FastBoard fastBoard = boards.remove(player.getUniqueId());
    fastBoard.delete();
  }

}
