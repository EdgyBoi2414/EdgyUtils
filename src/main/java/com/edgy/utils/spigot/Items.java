package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.DebugLogger;
import com.edgy.utils.shared.Messages;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;

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

    /**
     * Constructs an ItemBuilder from an existing ItemStack.
     * 
     * @param item The ItemStack to modify.
     */
    private ItemBuilder(ItemStack item) {
      this.item = item;
      this.meta = item.getItemMeta();
    }

    /**
     * Constructs an ItemBuilder from a Material.
     * 
     * @param material The Material to base the item on.
     */
    private ItemBuilder(Material material) {
      this.item = new ItemStack(material);
      this.meta = item.getItemMeta();
    }

    /**
     * Modifies the ItemStack using a Consumer.
     * 
     * @param itemConsumer The Consumer to modify the ItemStack.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder modify(Consumer<ItemStack> itemConsumer) {
      itemConsumer.accept(item);
      return this;
    }

    /**
     * Set the ItemMeta of the ItemStack.
     * 
     * @param meta The ItemMeta to modify.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder meta(ItemMeta meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Modifies the ItemMeta using a Consumer.
     * 
     * @param metaConsumer The Consumer to modify the ItemMeta.
     * @return This ItemBuilder for chaining.
     */
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

    /**
     * Sets the lore using a key processed by the Messages system with no
     * arguments.
     *
     * @param lore The message key for the lore.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder lore(String lore) {
      return lore(lore, new HashMap<>());
    }

    /**
     * Sets the lore using a key processed by the Messages system with provided
     * arguments.
     *
     * @param lore The message key for the lore.
     * @param args The arguments to substitute into the message.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder lore(String lore, Map<String, Object> args) {
      meta.setLore(messages.stringList(lore, args));
      return this;
    }

    /**
     * Sets the lore using a List of Strings.
     * 
     * @param lore The List of Strings to set as lore.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder lore(List<String> lore) {
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

    /**
     * Enchants the item with the specified enchantment and level.
     * 
     * @param enchantment The enchantment to add.
     * @param level       The level of the enchantment.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
      meta.addEnchant(enchantment, level, true);
      return this;
    }

    /**
     * Adds item flags to the item.
     * 
     * @param flags The flags to add.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder flags(ItemFlag... flags) {
      meta.addItemFlags(flags);
      return this;
    }

    /**
     * Sets the amount of the item.
     * 
     * @param amount The amount to set.
     * @return This ItemBuilder for chaining.
     */
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

    /**
     * Sets the durability of the item.
     * 
     * @param durability The durability to set.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder durability(int durability) {
      if (meta instanceof Damageable) {
        ((Damageable) meta).setDamage(durability);
      } else {
        DebugLogger.warning("This item is not damageable!");
      }
      return this;
    }

    /**
     * Sets the color of the item if it is a leather armor item.
     * 
     * @param color The color to set.
     * @return This ItemBuilder for chaining.
     */
    public ItemBuilder dye(Color color) {
      ((LeatherArmorMeta) this.meta).setColor(color);
      return this;
    }

    /**
     * Builds the ItemStack.
     * 
     * @return The built ItemStack.
     */
    public ItemStack build() {
      item.setItemMeta(meta);
      item.setAmount(amount);
      return item;
    }
  }

}
