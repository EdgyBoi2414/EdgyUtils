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

/**
 * Utility class for creating and modifying ItemStacks in a Spigot environment.
 * Provides static methods for common item creation patterns and a fluent
 * ItemBuilder for detailed customization.
 */
public class Items {

  /**
   * Creates a decorative ItemStack with a blank name, typically used as a
   * placeholder or UI element.
   *
   * @param material The material of the decorative item.
   * @return A new ItemStack with a blank display name.
   */
  public static ItemStack decoration(Material material) {
    return new ItemBuilder(material)
        .rawName(" ")
        .build();
  }

  /**
   * Creates an ItemBuilder for a given Material.
   *
   * @param material The material to base the item on.
   * @return A new ItemBuilder instance.
   */
  public static ItemBuilder builder(Material material) {
    return new ItemBuilder(material);
  }

  /**
   * Creates an ItemBuilder from an existing ItemStack.
   *
   * @param itemStack The ItemStack to modify.
   * @return A new ItemBuilder instance wrapping the provided ItemStack.
   */
  public static ItemBuilder builder(ItemStack itemStack) {
    return new ItemBuilder(itemStack);
  }

  /**
   * Creates a player head ItemStack with the specified PlayerProfile.
   *
   * @param playerProfile The profile containing the skin data for the head.
   * @return An ItemBuilder for a player head item.
   */
  public static ItemBuilder head(PlayerProfile playerProfile) {
    return new ItemBuilder(Material.PLAYER_HEAD)
        .meta(itemMeta -> {
          SkullMeta skullMeta = (SkullMeta) itemMeta;
          skullMeta.setOwnerProfile(playerProfile);
        });
  }

  /**
   * A fluent builder for creating and customizing ItemStacks.
   */
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

    /**
     * Sets the raw display name of the item without processing through the Messages
     * system.
     *
     * @param name The raw name to set.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder rawName(String name) {
      meta.setDisplayName(messages.string(name));
      return this;
    }

    /**
     * Sets the display name using a key processed by the Messages system with no
     * arguments.
     *
     * @param name The message key for the name.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder name(String name) {
      return name(name, new HashMap<>());
    }

    /**
     * Sets the display name using a key processed by the Messages system with
     * provided arguments.
     *
     * @param name The message key for the name.
     * @param args The arguments to substitute into the message.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder name(String name, Map<String, Object> args) {
      meta.setDisplayName(messages.string(name, args));
      return this;
    }

    /**
     * Sets the lore using raw strings without processing through the Messages
     * system.
     *
     * @param lore The raw lore lines to set.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder rawLore(String... lore) {
      meta.setLore(Arrays.stream(lore).map(messages::string).collect(Collectors.toList()));
      return this;
    }

    public ItemBuilder lore(
        String lore) {
      return lore(lore, new HashMap<>());
    }

    public ItemBuilder lore(
        String lore,
        Map<String, Object> args) {
      meta.setLore(messages.stringList(lore, args));
      return this;
    }

    public ItemBuilder lore(
        List<String> lore) {
      meta.setLore(lore.stream().map(messages::string).collect(Collectors.toList()));
      return this;
    }

    /**
     * Appends additional lore lines to the existing lore.
     *
     * @param lore The lore lines to append.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder appendLore(List<String> lore) {
      List<String> currentLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
      currentLore.addAll(lore.stream().map(messages::string).collect(Collectors.toList()));
      meta.setLore(currentLore);
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

    /**
     * Applies a potion effect to the item if it is a potion.
     *
     * @param type     The type of potion effect.
     * @param extended Whether the effect is extended duration.
     * @param upgraded Whether the effect is upgraded (e.g., level II).
     * @return This ItemBuilder for chaining.
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder potionEffect(PotionType type, boolean extended, boolean upgraded) {
      if (meta instanceof PotionMeta) {
        PotionMeta potionMeta = (PotionMeta) meta;
        potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
      } else {
        DebugLogger.warning("Cannot apply potion effect: Item is not a potion!");
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
      ((LeatherArmorMeta) this.meta).setColor(color);
      return this;
    }

    public ItemStack build() {
      item.setItemMeta(meta);
      item.setAmount(amount);
      return item;
    }
  }

}
