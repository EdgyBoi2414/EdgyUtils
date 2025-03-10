package com.edgy.utils.spigot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Inventories {

  public static int getSlot(Inventory inventory, ItemStack itemStack) {
    for (int i = 0; i < inventory.getSize(); i++) {
      ItemStack item = inventory.getItem(i);
      if (item != null && item.equals(itemStack)) {
        return i;
      }
    }
    return -1;
  }

  public static int getEmptySlots(Inventory inventory) {
    int emptySlots = 0;
    for (int i = 0; i < inventory.getSize(); i++) {
      ItemStack item = inventory.getItem(i);
      if (item == null || item.getType().isAir()) {
        emptySlots++;
      }
    }
    return emptySlots;
  }

}
