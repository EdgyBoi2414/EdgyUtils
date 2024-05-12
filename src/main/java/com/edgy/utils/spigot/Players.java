package com.edgy.utils.spigot;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class Players {

  public static void reset(Player player) {
    player.setExp(0);
    player.setLevel(0);
    player.setHealth(20);
    player.setSaturation(20);
    player.setFoodLevel(20);
    player.setFireTicks(0);
    player.getActivePotionEffects()
        .stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);

    player.getInventory().clear();
  }

  public static void scatter(List<Player> players, World world, int centerX, int centerZ, int range) {
    int minX = centerX - (range / 2);
    int maxX = centerX + (range / 2);
    int minZ = centerZ - (range / 2);
    int maxZ = centerZ + (range / 2);

    int randX = ThreadLocalRandom.current().nextInt(minX, maxX);
    int randZ = ThreadLocalRandom.current().nextInt(minZ, maxZ);

    for (Player player : players) {
      Location locationXZ = new Location(world, randX, 0, randZ);
      Location locationXYZ = new Location(world, locationXZ.getX(), world.getHighestBlockYAt(locationXZ), locationXZ.getZ());

      Material underBlock = locationXYZ.getBlock().getRelative(BlockFace.DOWN).getType();

      if (underBlock.isAir() || underBlock.equals(Material.LAVA)) {
        locationXYZ.getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
      }

      player.teleport(locationXYZ);
    }
  }

}
