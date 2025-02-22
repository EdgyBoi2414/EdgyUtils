package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.DebugLogger;
import com.edgy.utils.shared.Messages;
import com.edgy.utils.spigot.tinyprotocol.Reflection;
import com.edgy.utils.spigot.tinyprotocol.Reflection.FieldAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomSkullsEvent.SkullTextureType;

public class Items {

  public static ItemStack decoration(
      Material material
  ) {
    return new ItemBuilder(material)
        .rawName(" ")
        .build();
  }

  public static ItemBuilder builder(Material material) {
    return new ItemBuilder(material);
  }

  public static ItemBuilder builder(ItemStack itemStack) {
    return new ItemBuilder(itemStack);
  }

  public static ItemBuilder head(PlayerProfile playerProfile) {
    return new ItemBuilder(Material.PLAYER_HEAD)
        .meta(itemMeta -> {
          SkullMeta skullMeta = (SkullMeta) itemMeta;
          skullMeta.setOwnerProfile(playerProfile);
        });
  }

  public static class ItemBuilder {

    private final Messages<?> messages = EdgyUtils.bukkit().messages();
    private final ItemStack item;
    private int amount = 1;
    private ItemMeta meta;

    private ItemBuilder(ItemStack item) {
      this.item = item;
      this.meta = item.getItemMeta();
    }

    private ItemBuilder(Material material) {
      this.item = new ItemStack(material);
      this.meta = item.getItemMeta();
    }

    public ItemBuilder modify(Consumer<ItemStack> itemConsumer) {
      itemConsumer.accept(item);
      return this;
    }

    public ItemBuilder meta(ItemMeta meta) {
      this.meta = meta;
      return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
      metaConsumer.accept(meta);
      return this;
    }

    public ItemBuilder rawName(String name) {
      meta.setDisplayName(
          messages.string(name)
      );
      return this;
    }

    public ItemBuilder name(String name) {
      return name(name, new HashMap<>());
    }

    public ItemBuilder name(
        String name,
        Map<String, Object> args
    ) {
      meta.setDisplayName(messages.string(name, args));
      return this;
    }

    public ItemBuilder rawLore(String... lore) {
      meta.setLore(Arrays.stream(lore).map(messages::string).collect(Collectors.toList()));
      return this;
    }

    public ItemBuilder lore(
        String lore
    ) {
      return lore(lore, new HashMap<>());
    }

    public ItemBuilder lore(
        String lore,
        Map<String, Object> args
    ) {
      meta.setLore(messages.stringList(lore, args));
      return this;
    }

    public ItemBuilder lore(
        List<String> lore
    ) {
      meta.setLore(lore.stream().map(messages::string).collect(Collectors.toList()));
      return this;
    }

    public ItemBuilder appendLore(
        List<String> lore
    ) {
      if (!meta.hasLore()) {
        return lore(lore);
      }

      assert meta.getLore() != null;
      List<String> loreList = new ArrayList<>(meta.getLore());
      loreList.addAll(lore.stream().map(messages::string).collect(Collectors.toList()));
      meta.setLore(loreList);

      return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
      meta.addEnchant(enchantment, level, true);
      return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
      meta.addItemFlags(flags);
      return this;
    }

    public ItemBuilder amount(int amount) {
      this.amount = amount;
      return this;
    }


    @SuppressWarnings("deprecation")
    public ItemBuilder potionEffect(PotionType type, boolean bool1, boolean bool2) {
      try {
        PotionMeta potionMeta = (PotionMeta) meta;
        potionMeta.setBasePotionData(new PotionData(type, bool1, bool2));
      } catch (Exception err) {
        DebugLogger.warning("This item is not a potion!");
      }
      return this;
    }

    public ItemBuilder durability(int durability) {
      if (meta instanceof Damageable) {
        ((Damageable) meta).setDamage(durability);
      } else {
        DebugLogger.warning("This item is not damageable!");
      }
      return this;
    }

    public ItemBuilder dye(Color color) {
      ((LeatherArmorMeta)this.meta).setColor(color);
      return this;
    }

    public ItemStack build() {
      item.setItemMeta(meta);
      item.setAmount(amount);
      return item;
    }
  }

}
