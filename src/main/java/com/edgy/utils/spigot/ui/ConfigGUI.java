package com.edgy.utils.spigot.ui;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.geyser.Bedrock;
import com.edgy.utils.shared.DebugLogger;
import com.edgy.utils.shared.Messages;
import com.edgy.utils.spigot.Config;
import com.edgy.utils.spigot.ui.element.BedrockGUIElement;
import com.edgy.utils.spigot.ui.element.GUIElement;
import com.edgy.utils.spigot.ui.function.ElementParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.cumulus.form.SimpleForm;
import org.incendo.interfaces.core.click.ClickContext;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.core.transform.Transform;
import org.incendo.interfaces.core.util.Vector2;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.transform.PaperTransform;
import org.incendo.interfaces.paper.type.ChestInterface;
import org.incendo.interfaces.paper.utils.PaperUtils;

public class ConfigGUI {

  private static final List<UUID> ignoreClose = new ArrayList<>();

  public static ConfigGUI.Builder builder(Player player, Config config) {
    return new ConfigGUI.Builder(player, config);
  }

  public static void ignoreClose(Player player) {
    ignoreClose.add(player.getUniqueId());
  }

  public static void preventClose(Player player) {
    ignoreClose.remove(player.getUniqueId());
  }

  public static boolean shouldIgnoreClose(Player player) {
    return ignoreClose.contains(player.getUniqueId());
  }

  private final Messages<CommandSender> messages = EdgyUtils.bukkit().messages();
  private final Player player;
  private final Config config;
  private final List<Function<Map<String, Object>, CompletableFuture<Transform<ChestPane, PlayerViewer>>>> customTransforms;
  private final ElementParser<GUIElement> itemParser;
  private final ElementParser<BedrockGUIElement> bedrockButtonParser;
  private final BiFunction<Player, Map<String, Object>, CompletableFuture<List<BedrockGUIElement>>> bedrockTransform;
  private final BiFunction<Player, ConfigurationSection, CompletableFuture<Map<String, Object>>> argumentParser;
  private final Map<String, BiFunction<Map<String, Object>, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>, Map<String, Object>>> requiredItems = new HashMap<>();
  private final Map<String, Object> arguments = new HashMap<>();
  private final Runnable onClose;
  private final boolean noCloseButton;
  private ConfigGUI(
      Player player,
      Config config,
      List<Function<Map<String, Object>, CompletableFuture<Transform<ChestPane, PlayerViewer>>>> customTransforms,
      ElementParser<GUIElement> itemParser,
      BiFunction<Player, ConfigurationSection, CompletableFuture<Map<String, Object>>> argumentParser,
      Map<String, BiFunction<Map<String, Object>, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>, Map<String, Object>>> requiredItems,
      ElementParser<BedrockGUIElement> bedrockButtonParser,
      BiFunction<Player, Map<String, Object>, CompletableFuture<List<BedrockGUIElement>>> bedrockTransform,
      Runnable onClose,
      boolean noCloseButton
  ) {
    this.player = player;
    this.config = config;
    this.customTransforms = customTransforms;
    this.itemParser = itemParser;
    this.argumentParser = argumentParser;
    this.requiredItems.putAll(requiredItems);
    this.bedrockButtonParser = bedrockButtonParser;
    this.bedrockTransform = bedrockTransform;
    this.noCloseButton = noCloseButton;
    if (!noCloseButton) {
      this.requiredItems.put("close", (args, context) -> {
        ignoreClose(context.viewer().player());
        context.viewer().close();
        return args;
      });
    }

    this.onClose = onClose;

    String title = config.get().getString("title", "- TITLE MISSING -");

    if (Bedrock.isBedrock() && Bedrock.isGeyserPlayer(player.getUniqueId()) && config.get()
        .getBoolean("bedrock.enabled", false)) {
      if (argumentParser != null) {
        DebugLogger.info("Handling arguments...");
        argumentParser.apply(player, config.get())
            .whenComplete((args, err) -> {
              if (err != null) {
                throw new RuntimeException(err);
              }

              bedrock(title, config, args);
            });
      } else {
        DebugLogger.info("No argument parser provided");
        bedrock(title, config, new HashMap<>());
      }
      return;
    }

    int rows = config.get().getInt("rows", 3);
    if (rows > 6 || rows < 1) {
      throw new IllegalArgumentException("Invalid rows for GUI " + config.get().getName() + "!");
    }
    Material fill;
    try {
      fill = Material.valueOf(
          config.get().getString("fill", "AIR").toUpperCase());
    } catch (IllegalArgumentException err) {
      throw new IllegalArgumentException(
          "Invalid fill for GUI " + config.get().getName() + "! (Must be a valid material)");
    }

    ConfigurationSection itemsSection = config.get().getConfigurationSection("items");

    List<ConfigurationSection> itemSections;
    if (itemsSection == null) {
      itemSections = new ArrayList<>();
    } else {
      itemSections = itemsSection.getKeys(false)
          .stream()
          .map(itemsSection::getConfigurationSection)
          .collect(Collectors.toList());
    }

    ItemStack fillStack = new ItemStack(fill);
    if (fill != Material.AIR) {
      ItemMeta meta = Objects.requireNonNull(fillStack.getItemMeta());
      meta.setDisplayName(" ");
      fillStack.setItemMeta(meta);
    }

    ChestInterface.Builder builder = ChestInterface.builder()
        .title(messages.component(title))
        .rows(rows)
        .clickHandler(ClickHandler.cancel())
        .addCloseHandler((event, view) -> onClose.run());

    List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
    List<Transform<ChestPane, PlayerViewer>> transforms = new ArrayList<>();

    DebugLogger.info("Handling required items: " + this.requiredItems.size());
    for (String path : this.requiredItems.keySet()) {
      DebugLogger.info("Handling required item: " + path);
      ConfigurationSection section = config.get().getConfigurationSection(path);
      if (section == null) {
        EdgyUtils.logger()
            .severe("Required item " + path + " is missing in GUI " + config.get().getName() + "!");
        continue;
      }
      completableFutures.add(
          getItemStack(player, section)
              .exceptionally(throwable -> {
                throw new RuntimeException(throwable);
              })
              .thenAccept(element -> transforms.add((pane, view) -> {
                List<Integer> slots = section.getIntegerList("slots");
                if (slots.isEmpty()) {
                  throw new IllegalArgumentException(
                      "Invalid slots for " + path + " in GUI " + config.get().getName()
                          + "! (List was empty)"
                  );
                }
                for (Integer slot : slots) {
                  Vector2 grid = PaperUtils.slotToGrid(slot);
                  pane = pane.element(
                      ItemStackElement.of(
                          element.item(),
                          (context) -> {
                            arguments.putAll(
                                this.requiredItems.get(path).apply(arguments, context));
                            context.view().update();
                          }
                      ),
                      grid.x(), grid.y());
                }

                return pane;
              }))
      );
    }

    DebugLogger.info("Handling items: " + itemSections.size());
    for (ConfigurationSection itemSection : itemSections) {
      DebugLogger.info("Handling item: " + itemSection.getName());
      completableFutures.add(
          getItemStack(player, itemSection)
              .thenAccept(element -> transforms.add((pane, view) -> {
                List<Integer> slots = itemSection.getIntegerList("slots");
                if (slots.isEmpty()) {
                  throw new IllegalArgumentException(
                      "Invalid item slots for GUI " + config.get().getName()
                          + "! (List was empty)");
                }
                for (Integer slot : slots) {
                  Vector2 grid = PaperUtils.slotToGrid(slot);
                  pane = pane.element(
                      ItemStackElement.of(
                          element.item(),
                          (context) -> {
                            Player viewer = context.viewer().player();
                            List<String> commands = itemSection.getStringList("commands");
                            element.clickHandler().accept(context);
                            if (commands.isEmpty()) {
                              return;
                            }
                            for (String command : commands) {
                              Bukkit.dispatchCommand(
                                  Bukkit.getConsoleSender(),
                                  command.replace("{player}", viewer.getName())
                              );
                            }
                            ignoreClose(player);
                            context.viewer().close();
                          }
                      ), grid.x(), grid.y());
                }

                return pane;
              }))
      );
    }

    if (argumentParser != null) {
      DebugLogger.info("Handling arguments");
      completableFutures.add(
          argumentParser.apply(player, config.get())
              .thenAccept(this.arguments::putAll)
      );
    }

    DebugLogger.info("Handling custom transforms: " + customTransforms.size());
    for (Function<Map<String, Object>, CompletableFuture<Transform<ChestPane, PlayerViewer>>> function : customTransforms) {
      completableFutures.add(function.apply(arguments)
          .exceptionally(throwable -> {
            EdgyUtils.logger()
                .log(Level.SEVERE, "Error building GUI" + config.get().getName(), throwable);
            return null;
          })
          .thenAccept(transform -> {
            if (transform != null) {
              transforms.add(transform);
            }
          }));
    }

    ChestInterface.Builder finalBuilder = builder;
    CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{}))
        .exceptionally(throwable -> {
          EdgyUtils.logger()
              .log(Level.SEVERE, "Error building GUI" + config.get().getName(), throwable);
          return null;
        })
        .thenAccept((v) -> {
          ChestInterface.Builder finalFinalBuilder = finalBuilder;
          finalFinalBuilder = finalFinalBuilder.addTransform(PaperTransform.chestFill(
              ItemStackElement.of(fillStack)
          ));

          for (Transform<ChestPane, PlayerViewer> transform : transforms) {
            finalFinalBuilder = finalFinalBuilder.addTransform(transform);
          }

          DebugLogger.info("Opening gui...");
          finalFinalBuilder.build().open(PlayerViewer.of(player));
        });
  }

  private void bedrock(String title, Config config, Map<String, Object> args) {
    final List<BiConsumer<Player, Map<String, Object>>> buttonIds = new ArrayList<>();

    ConfigurationSection bedrockSection = Objects.requireNonNull(
        config.get().getConfigurationSection("bedrock"));

    SimpleForm.Builder builder = SimpleForm.builder()
        .title(title)
        .closedResultHandler(onClose);

    String description = config.get().getString("description");
    if (description != null) {
      builder = builder.content(messages.string(description));
    }

    List<String> ignoredRequiredItems = bedrockSection.getStringList("ignored_required_items");

    DebugLogger.info("Handling required items: " + (requiredItems.keySet().size()
        - ignoredRequiredItems.size()));

    for (String key : requiredItems.keySet()) {
      if (ignoredRequiredItems.contains(key)) {
        continue;
      }

      ConfigurationSection section = config.get().getConfigurationSection(key);
      if (section == null) {
        EdgyUtils.logger()
            .warning("Required item " + key + " is missing in GUI " + config.get().getName() + "!");
        continue;
      }
      String name = messages.string(section.getString("name", "- NAME MISSING -"));

      builder = builder.button(name);
      buttonIds.add((p, a) -> {
      });
    }

    List<ConfigurationSection> buttonSections;
    if (config.get().isConfigurationSection("items")) {
      ConfigurationSection itemSection = Objects.requireNonNull(
          config.get().getConfigurationSection("items")
      );

      buttonSections = itemSection.getKeys(false)
          .stream()
          .map(itemSection::getConfigurationSection)
          .collect(Collectors.toList());

      DebugLogger.info("Handling items:" + buttonSections.size());
    } else {
      buttonSections = new ArrayList<>();
      DebugLogger.info("No items to handle");
    }

    List<CompletableFuture<Void>> elementFutures = new ArrayList<>();
    List<BedrockGUIElement> elements = new ArrayList<>();
    for (ConfigurationSection buttonSection : buttonSections) {
      DebugLogger.info("Handling item: " + buttonSection.getName());
      elementFutures.add(
          getButton(player, buttonSection)
              .thenAccept(elements::add)
      );
    }

    if (bedrockTransform != null) {
      DebugLogger.info("Handling bedrock transform");
      elementFutures.add(
          bedrockTransform.apply(player, args)
              .thenAccept(elements::addAll)
      );
    }

    DebugLogger.info("Building form | Waiting for " + elementFutures.size() + " futures...");
    AtomicReference<SimpleForm.Builder> atomicBuilderReference = new AtomicReference<>(builder);
    CompletableFuture.allOf(elementFutures.toArray(new CompletableFuture[]{}))
        .exceptionally(throwable -> {
          EdgyUtils.logger()
              .log(Level.SEVERE, "Error building GUI" + config.get().getName(), throwable);
          return null;
        })
        .thenAccept((v) -> {
          DebugLogger.info("Finalising form...");
          SimpleForm.Builder finalBuilder = atomicBuilderReference.get();
          for (BedrockGUIElement element : elements) {
            finalBuilder = finalBuilder.button(element.name(), element.formImage());
            buttonIds.add(element.consumer());
          }

          finalBuilder = finalBuilder.validResultHandler((simpleForm, simpleFormResponse) -> {
            int index = simpleFormResponse.clickedButtonId();
            buttonIds.get(index).accept(player, args);
          });

          DebugLogger.info("Sending form...");
          Bedrock.sendForm(player.getUniqueId(), finalBuilder.build());
          DebugLogger.info("Form sent.");
        });
  }


  private CompletableFuture<BedrockGUIElement> getButton(
      Player player,
      ConfigurationSection section
  ) {
    CompletableFuture<BedrockGUIElement> guiElementFuture = new CompletableFuture<>();
    CompletableFuture<BedrockGUIElement> parserFuture;
    if (bedrockButtonParser != null) {
      parserFuture = bedrockButtonParser.apply(player, section);
    } else {
      parserFuture = CompletableFuture.completedFuture(new BedrockGUIElement(null, null, null));
    }

    parserFuture.whenComplete((element, throwable) -> {
      if (throwable != null) {
        throw new RuntimeException(throwable);
      }

      String name = section.getString("name", "- NAME MISSING -");
      List<String> commands = section.getStringList("commands");

      if (element.name() == null) {
        element = new BedrockGUIElement(messages.string(name), element.formImage(),
            element.consumer());
      }

      if (element.consumer() == null) {
        element = new BedrockGUIElement(element.name(), element.formImage(), (p, a) -> {
          for (String command : commands) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                command.replace("{player}", p.getName())
            );
          }
        });
      }

      DebugLogger.info("Completing future for " + section.getName());
      guiElementFuture.complete(element);
    });

    return guiElementFuture;
  }

  private CompletableFuture<GUIElement> getItemStack(Player player, ConfigurationSection section) {
    CompletableFuture<GUIElement> guiElementFuture = new CompletableFuture<>();
    CompletableFuture<GUIElement> parserFuture;
    if (itemParser != null) {
      parserFuture = itemParser.apply(player, section);
    } else {
      parserFuture = CompletableFuture.completedFuture(new GUIElement(null, null));
    }

    parserFuture.whenComplete((element, throwable) -> {
      String name = section.getString("name", "- NAME MISSING -");
      String materialName = section.getString("material", "---");
      List<String> lore = section.getStringList("lore");
      int customModelData = section.getInt("custom_model_data", 0);
      boolean glow = section.getBoolean("glow", false);

      Material material;
      try {
        material = Material.valueOf(materialName.toUpperCase());
      } catch (IllegalArgumentException err) {
        throw new IllegalArgumentException(
            "Invalid material for GUI " + section.getName() + "! (Must be a valid material)"
        );
      }

      if (element.item() == null) {
        element = new GUIElement(new ItemStack(material), element.clickHandler());
      }

      ItemMeta meta = element.item().getItemMeta();
      if (meta == null) {
        throw new IllegalArgumentException(
            "Invalid material for GUI " + section.getName() + "! (Error getting item meta)");
      }
      meta.setDisplayName(name.isEmpty() ? " " : messages.string(name));
      meta.setLore(
          lore.stream()
              .map(messages::string)
              .collect(Collectors.toList())
      );
      if (glow && !meta.hasEnchants()) {
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }

      meta.setCustomModelData(customModelData);

      element.item().setItemMeta(meta);
      guiElementFuture.complete(element);
    });

    return guiElementFuture;
  }

  public static class Builder {

    private final Player player;
    private final Config config;

    private ElementParser<GUIElement> itemParser;
    private ElementParser<BedrockGUIElement> bedrockButtonParser;
    private BiFunction<Player, ConfigurationSection, CompletableFuture<Map<String, Object>>> argumentParser;
    private Map<String, BiFunction<Map<String, Object>, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>, Map<String, Object>>> requiredItems = new HashMap<>();
    private BiFunction<Player, Map<String, Object>, CompletableFuture<List<BedrockGUIElement>>> bedrockTransform;
    private final List<Function<Map<String, Object>, CompletableFuture<Transform<ChestPane, PlayerViewer>>>> transforms = new ArrayList<>();

    private Runnable onClose = () -> {};
    private boolean noCloseButton = false;

    private Builder(Player player, Config config) {
      this.player = player;
      this.config = config;
    }

    public Builder parseArguments(
        BiFunction<Player, ConfigurationSection, CompletableFuture<Map<String, Object>>> argumentParser
    ) {
      this.argumentParser = argumentParser;
      return this;
    }

    public Builder parseItem(ElementParser<GUIElement> parser) {
      this.itemParser = parser;
      return this;
    }

    public Builder parseBedrockButton(ElementParser<BedrockGUIElement> parser) {
      this.bedrockButtonParser = parser;
      return this;
    }

    public Builder requiredItem(
        String path,
        BiFunction<Map<String, Object>, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>, Map<String, Object>> onClick
    ) {
      this.requiredItems.put(path, onClick);
      return this;
    }

    public Builder addTransform(
        Function<Map<String, Object>, CompletableFuture<Transform<ChestPane, PlayerViewer>>> transform) {
      this.transforms.add(transform);
      return this;
    }

    public Builder addBedrockButtons(
        BiFunction<Player, Map<String, Object>, CompletableFuture<List<BedrockGUIElement>>> transform
    ) {
      this.bedrockTransform = transform;
      return this;
    }

    public Builder onClose(Runnable onClose) {
      this.onClose = onClose;
      return this;
    }

    public Builder noCloseButton(boolean noCloseButton) {
      this.noCloseButton = noCloseButton;
      return this;
    }

    public ConfigGUI build() {
      return new ConfigGUI(
          player,
          config,
          transforms,
          itemParser,
          argumentParser,
          requiredItems,
          bedrockButtonParser,
          bedrockTransform,
          onClose,
          noCloseButton
      );
    }
  }

}
