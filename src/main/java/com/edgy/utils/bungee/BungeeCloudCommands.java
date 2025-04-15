package com.edgy.utils.bungee;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.DebugLogger;
import java.util.List;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.bungee.BungeeCommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class BungeeCloudCommands {

  private final static BungeeAudiences BUNGEE_AUDIENCES = BungeeAudiences.create(EdgyUtils.bungee());
  private final static AudienceProvider<CommandSender> AUDIENCE_PROVIDER = new AudienceProvider<>() {
    @Override
    public @NonNull Audience apply(@NotNull CommandSender sender) {
      return BUNGEE_AUDIENCES.sender(sender);
    }
  };

  private final static BungeeAbstractCommandContainer<CommandSender, CommandManager<CommandSender>> BUNGEE_DEBUG_CONTAINER = new BungeeAbstractCommandContainer<>() {
    @Override
    protected void registerCommands(CommandManager<CommandSender> commandManager) {
      commandManager.command(
          commandManager.commandBuilder("bungeeedgyutils", "beu", "butils")
              .literal("debug")
              .senderType(CommandSender.class)
              .handler(context -> {
                DebugLogger.enabled(!DebugLogger.enabled());
                EdgyUtils.bungee().messages().sendMessage(
                        context.sender(),
                    "Debug mode is now " + (DebugLogger.enabled() ? "enabled" : "disabled"));
              })
      );
    }
  };

  private static BungeeCommandManager<CommandSender> commandManager = null;

  public static void bungee(List<BungeeAbstractCommandContainer<CommandSender, BungeeCommandManager<CommandSender>>> containers) {
    final BungeeCommandManager<CommandSender> commandManager;
    final AnnotationParser<CommandSender> annotationParser;

    try {
      commandManager = new BungeeCommandManager<>(
              EdgyUtils.bungee(),
              ExecutionCoordinator.simpleCoordinator(),
              SenderMapper.identity()
      );
    } catch (Exception err) {
      EdgyUtils.logger().log(Level.SEVERE, "Failed to initialize command manager!", err);
      return;
    }

    annotationParser = new AnnotationParser<>(commandManager, CommandSender.class);

    MinecraftExceptionHandler.create(AUDIENCE_PROVIDER)
            .defaultHandlers()
            .decorator(
                    component ->
                            EdgyUtils.bungee()
                                    .messages()
                                    .component("<red><bold><sm_caps:error></bold> <dark_grey>→ <white>")
                                    .append(component.color(NamedTextColor.WHITE)))
            .registerTo(commandManager);

    String containerName = "Unknown";
    try {
      boolean parseAll = true;
      for (BungeeAbstractCommandContainer<CommandSender, BungeeCommandManager<CommandSender>> container : containers) {
        if (container.getClass().isAnnotationPresent(CommandContainer.class)) {
          containerName = container.getClass().getSimpleName();
          annotationParser.parse(container);
          parseAll = false;
        }
      }
      if (parseAll) {
        annotationParser.parseContainers();
      }
    } catch (Exception err) {
      EdgyUtils.logger().log(Level.SEVERE, "Failed to parse command containers! Bad Container: " + containerName, err);
    }

    for (BungeeAbstractCommandContainer<CommandSender, BungeeCommandManager<CommandSender>> container : containers) {
      container.registerCommands(commandManager);
    }

    BUNGEE_DEBUG_CONTAINER.registerCommands(commandManager);

    BungeeCloudCommands.commandManager = commandManager;
  }

  public static abstract class BungeeAbstractCommandContainer<S, C extends CommandManager<S>> {

    protected abstract void registerCommands(C commandManager);

  }

}
