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
 * Utility class for creating and manipulating Bukkit ItemStacks.
 */
public class Items {

  /**
   * Creates a decorative item with empty name.
   * 
   * @param material the material to use
   * @return a decoration item
   */
  public static ItemStack decoration(Material material) {
    return new ItemBuilder(material)
        .rawName(" ")
        .build();
  }

  /**
   * Creates a new ItemBuilder with the specified material.
   * 
   * @param material the material to use
   * @return a new ItemBuilder instance
   */
  public static ItemBuilder builder(Material material) {
    return new ItemBuilder(material);
  }

  /**
   * Creates a new ItemBuilder from an existing ItemStack.
   * 
   * @param itemStack the ItemStack to build upon
   * @return a new ItemBuilder instance
   */
  public static ItemBuilder builder(ItemStack itemStack) {
    return new ItemBuilder(itemStack);
  }

  /**
   * Creates a new player head ItemBuilder with the specified player profile.
   * 
   * @param playerProfile the player profile to use for the head
   * @return a new ItemBuilder instance configured as a player head
   */
  public static ItemBuilder head(PlayerProfile playerProfile) {
    return new ItemBuilder(Material.PLAYER_HEAD)
        .meta(itemMeta -> {
          SkullMeta skullMeta = (SkullMeta) itemMeta;
          skullMeta.setOwnerProfile(playerProfile);
        });
  }

  /**
   * Builder class for creating customized ItemStacks.
   */
  public static class ItemBuilder {

    private final Messages<?> messages = EdgyUtils.bukkit().messages();
    private final ItemStack item;
    private int amount = 1;
    private ItemMeta meta;

    /**
     * Creates a new ItemBuilder from an existing ItemStack.
     * 
     * @param item the ItemStack to build upon
     */
    private ItemBuilder(ItemStack item) {
      this.item = item;
      this.meta = item.getItemMeta();
    }

    /**
     * Creates a new ItemBuilder with the specified material.
     * 
     * @param material the material to use
     */
    private ItemBuilder(Material material) {
      this.item = new ItemStack(material);
      this.meta = item.getItemMeta();
    }

    /**
     * Applies a consumer directly to the ItemStack.
     * 
     * @param itemConsumer consumer to modify the ItemStack
     * @return this builder for chaining
     */
    public ItemBuilder modify(Consumer<ItemStack> itemConsumer) {
      itemConsumer.accept(item);
      return this;
    }

    /**
     * Sets a new ItemMeta on the builder.
     * 
     * @param meta the ItemMeta to set
     * @return this builder for chaining
     */
    public ItemBuilder meta(ItemMeta meta) {
      this.meta = meta;
      return this;
    }

    /**
     * Applies a consumer to modify the ItemMeta.
     * 
     * @param metaConsumer consumer to modify the ItemMeta
     * @return this builder for chaining
     */
    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
      metaConsumer.accept(meta);
      return this;
    }

    /**
     * Sets a raw display name without translation.
     * 
     * @param name the raw name to set
     * @return this builder for chaining
     */
    public ItemBuilder rawName(String name) {
      meta.setDisplayName(messages.string(name));
      return this;
    }

    /**
     * Sets a translated display name.
     * 
     * @param name the name key to translate
     * @return this builder for chaining
     */
    public ItemBuilder name(String name) {
      return name(name, new HashMap<>());
    }

    /**
     * Sets a translated display name with arguments.
     * 
     * @param name the name key to translate
     * @param args arguments for translation
     * @return this builder for chaining
     */
    public ItemBuilder name(String name, Map<String, Object> args) {
      meta.setDisplayName(messages.string(name, args));
      return this;
    }

    /**
     * Sets raw lore lines without translation.
     * 
     * @param lore array of lore lines
     * @return this builder for chaining
     */
    public ItemBuilder rawLore(String... lore) {
      meta.setLore(Arrays.stream(lore).map(messages::string).collect(Collectors.toList()));
      return this;
    }

    /**
     * Sets a translated lore.
     * 
     * @param lore the lore key to translate
     * @return this builder for chaining
     */
    public ItemBuilder lore(String lore) {
      return lore(lore, new HashMap<>());
    }

    /**
     * Sets a translated lore with arguments.
     * 
     * @param lore the lore key to translate
     * @param args arguments for translation
     * @return this builder for chaining
     */
    public ItemBuilder lore(String lore, Map<String, Object> args) {
      meta.setLore(messages.stringList(lore, args));
      return this;
    }

    /**
     * Sets lore from a list of strings.
     * 
     * @param lore list of lore lines
     * @return this builder for chaining
     */
    public ItemBuilder lore(List<String> lore) {
      meta.setLore(lore.stream().map(messages::string).collect(Collectors.toList()));
      return this;
    }

    /**
     * Appends lore lines to existing lore.
     * 
     * @param lore list of lore lines to append
     * @return this builder for chaining
     */
    public ItemBuilder appendLore(List<String> lore) {
      if (!meta.hasLore()) {
        return lore(lore);
      }

      List<String> loreList = new ArrayList<>(meta.getLore() != null ? meta.getLore() : new ArrayList<>());
      loreList.addAll(lore.stream().map(messages::string).collect(Collectors.toList()));
      meta.setLore(loreList);

      return this;
    }

    /**
     * Adds an enchantment to the item.
     * 
     * @param enchantment the enchantment to add
     * @param level       the level of the enchantment
     * @return this builder for chaining
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
      meta.addEnchant(enchantment, level, true);
      return this;
    }

    /**
     * Adds ItemFlags to the item.
     * 
     * @param flags the flags to add
     * @return this builder for chaining
     */
    public ItemBuilder flags(ItemFlag... flags) {
      meta.addItemFlags(flags);
      return this;
    }

    /**
     * Sets the item amount.
     * 
     * @param amount the amount to set
     * @return this builder for chaining
     */
    public ItemBuilder amount(int amount) {
      this.amount = amount;
      return this;
    }

    /**
     * Sets potion effect properties.
     * 
     * @param type     the potion type
     * @param extended whether the effect is extended
     * @param upgraded whether the effect is upgraded
     * @return this builder for chaining
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder potionEffect(PotionType type, boolean extended, boolean upgraded) {
      try {
        PotionMeta potionMeta = (PotionMeta) meta;
        potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
      } catch (Exception err) {
        DebugLogger.warning("This item is not a potion!");
      }
      return this;
    }

    /**
     * Sets item durability/damage.
     * 
     * @param durability the durability value to set
     * @return this builder for chaining
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
     * Sets color for leather armor items.
     * 
     * @param color the color to set
     * @return this builder for chaining
     */
    public ItemBuilder dye(Color color) {
      if (meta instanceof LeatherArmorMeta) {
        ((LeatherArmorMeta) meta).setColor(color);
      } else {
        DebugLogger.warning("This item is not leather armor!");
      }
      return this;
    }

    /**
     * Builds the final ItemStack.
     * 
     * @return the built ItemStack
     */
    public ItemStack build() {
      item.setItemMeta(meta);
      item.setAmount(amount);
      return item;
    }
  }
}