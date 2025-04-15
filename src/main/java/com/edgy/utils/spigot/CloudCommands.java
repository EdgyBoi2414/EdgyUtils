package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.DebugLogger;
import com.edgy.utils.shared.Messages;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;

import static net.kyori.adventure.text.Component.text;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

public class CloudCommands {

  private final static BukkitAudiences BUKKIT_AUDIENCES = BukkitAudiences.create(EdgyUtils.bukkit());
  private final static AudienceProvider<Source> AUDIENCE_PROVIDER = new AudienceProvider<>() {
    @Override
    public @NonNull Audience apply(@NonNull Source source) {
      return BUKKIT_AUDIENCES.sender(source.source());
    }
  };

  private final static AbstractCommandContainer<Source, PaperCommandManager<Source>> PAPER_DEFAULT_CONTAINER = new AbstractCommandContainer<>() {
    @Override
    protected void registerCommands(PaperCommandManager<Source> commandManager) {

      MinecraftHelp<Source> help = MinecraftHelp.<Source>builder()
              .commandManager(commandManager)
              .audienceProvider(AUDIENCE_PROVIDER)
              .commandPrefix("help")
              .colors(MinecraftHelp.helpColors(
                      Objects.requireNonNull(TextColor.fromHexString("#FF0000")),
                      NamedTextColor.WHITE,
                      Objects.requireNonNull(TextColor.fromHexString("#FF0000")),
                      NamedTextColor.GRAY,
                      NamedTextColor.DARK_GRAY
              ))
              .build();

      commandManager.command(
              commandManager.commandBuilder("help")
                      .optional("query", greedyStringParser(), DefaultValue.constant(""))
                      .handler(context -> {
                        help.queryCommands(context.get("query"), context.sender());
                      })
      );

      commandManager.command(
          commandManager.commandBuilder("edgyutils", "eu", "utils")
              .literal("debug")
              .senderType(Source.class)
              .handler(context -> {
                DebugLogger.enabled(!DebugLogger.enabled());
                context.sender().source().sendMessage(
                    "Debug mode is now " + (DebugLogger.enabled() ? "enabled" : "disabled"));
              })
      );

      commandManager.command(
          commandManager.commandBuilder("edgyutils", "eu", "utils")
              .literal("smallcaps")
              .senderType(Source.class)
              .required("text", greedyStringParser(), Description.of("Text to convert to small caps"))
              .handler(context -> {
                String text = context.get("text");

                TextComponent.Builder builder = text();
                builder.append(text(Messages.toSmallCaps(text))
                    .append(text(" [Click to Copy]").color(
                        TextColor.color(0x1B1B1B))));
                builder.clickEvent(ClickEvent.copyToClipboard(Messages.toSmallCaps(text)));
                builder.hoverEvent(HoverEvent.showText(text("Click to copy")));
                EdgyUtils.bukkit()
                        .messages()
                        .audience(context.sender().source())
                    .sendMessage(builder.build());
              })
      );
    }
  };



  private static PaperCommandManager<Source> commandManager = null;

  public static PaperCommandManager<Source> commandManager() {
    return commandManager;
  }

  @SafeVarargs
  public static void bukkit(
      AbstractCommandContainer<Source, PaperCommandManager<Source>>... container
  ) {
    bukkit(Arrays.asList(container));
  }

  public static void bukkit(
      List<AbstractCommandContainer<Source, PaperCommandManager<Source>>> containers
  ) {
    final PaperCommandManager<Source> commandManager;
    final AnnotationParser<Source> annotationParser;

    try {
      commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
              .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
              .buildOnEnable(EdgyUtils.bukkit());
    } catch (Exception err) {
      EdgyUtils.logger().log(Level.SEVERE, "Failed to initialize command manager!", err);
      return;
    }

    annotationParser = new AnnotationParser<>(commandManager, Source.class);

    MinecraftExceptionHandler.create(AUDIENCE_PROVIDER)
        .defaultHandlers()
        .decorator(
            component ->
                EdgyUtils.bukkit()
                    .messages()
                    .component("<red><sm_caps:error> <dark_grey>→ <white>"))
        .registerTo(commandManager);

    try {
      boolean parseAll = true;
      for (AbstractCommandContainer<Source, PaperCommandManager<Source>> container : containers) {
        if (container.getClass().isAnnotationPresent(CommandContainer.class)) {
          annotationParser.parse(container);
          parseAll = false;
        }
      }

      if (parseAll) {
        annotationParser.parseContainers();
      }
    } catch (Exception err) {
      EdgyUtils.logger().log(Level.SEVERE, "Failed to parse command containers!", err);
    }

    for (AbstractCommandContainer<Source, PaperCommandManager<Source>> container : containers) {
      container.registerCommands(commandManager);
    }

    PAPER_DEFAULT_CONTAINER.registerCommands(commandManager);

    CloudCommands.commandManager = commandManager;
  }

  public static abstract class AbstractCommandContainer<S, C extends CommandManager<S>> {

    protected abstract void registerCommands(C commandManager);

  }

}
