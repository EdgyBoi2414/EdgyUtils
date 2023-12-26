package com.edgy.utils.spigot;

import java.util.ArrayList;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerJoinEvent;

public class Worlds {

  public static ArrayList<Chunk> getChunks(Location loc) {
    ArrayList<Chunk> chunks = new ArrayList<Chunk>();

    Chunk c = loc.getChunk();

    Location center = new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4).add(7, 0, 7);

    for (int x = -64; x <= 64; x+=8) {
      for (int z = -64; z <= 64; z+=8) {
        Location loc2 = new Location(c.getWorld(), center.getX() + x, center.getY(), center.getZ() + z);
        if (loc2.getChunk() != c) {
          if (!chunks.contains(loc2.getChunk())) {
            chunks.add(loc2.getChunk());
          }
        }
      }
    }

    return chunks;
  }

}
