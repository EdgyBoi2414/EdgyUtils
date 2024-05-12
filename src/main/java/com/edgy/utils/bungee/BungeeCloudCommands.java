package com.edgy.utils.bungee;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.processing.CommandContainer;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.DebugLogger;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class BungeeCloudCommands {

  private final static BungeeAbstractCommandContainer<CommandSender, CommandManager<CommandSender>> BUNGEE_DEBUG_CONTAINER = new BungeeAbstractCommandContainer<>() {
    @Override
    protected void registerCommands(CommandManager<CommandSender> commandManager) {
      commandManager.command(
          commandManager.commandBuilder("edgyutils", "eu")
              .literal("debug")
              .senderType(CommandSender.class)
              .handler(context -> {
                DebugLogger.enabled(!DebugLogger.enabled());
                context.getSender().sendMessage(
                    "Debug mode is now " + (DebugLogger.enabled() ? "enabled" : "disabled"));
              })
      );
    }
  };

  private static CommandManager<CommandSender> commandManager = null;

  public static void bungee(
      List<BungeeAbstractCommandContainer<CommandSender, BungeeCommandManager<CommandSender>>> containers) {
    final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
        AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();
    final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
    final BungeeCommandManager<CommandSender> commandManager;
    final AnnotationParser<CommandSender> annotationParser;
    final BungeeAudiences bungeeAudiences = BungeeAudiences.create(EdgyUtils.bungee());

    try {
      commandManager = new BungeeCommandManager<>(
          EdgyUtils.bungee(),
          executionCoordinatorFunction,
          mapperFunction,
          mapperFunction
      );
    } catch (Exception e) {
      EdgyUtils.bungee().getLogger().log(Level.SEVERE, "Failed to initialize command manager!");
      e.printStackTrace();
      return;
    }

    final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
        CommandMeta.simple()
            .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
            .build();
    annotationParser = new AnnotationParser<>(
        commandManager,
        CommandSender.class,
        commandMetaFunction
    );

    AudienceProvider<CommandSender> audience = new AudienceProvider<>() {
      @Override
      public @NonNull Audience apply(@NotNull CommandSender sender) {
        return bungeeAudiences.sender(sender);
      }
    };

    new MinecraftExceptionHandler<CommandSender>()
        .withInvalidSyntaxHandler()
        .withInvalidSenderHandler()
        .withNoPermissionHandler()
        .withArgumentParsingHandler()
        .withCommandExecutionHandler()
        .apply(commandManager, audience);

    try {
      boolean parseAll = true;
      for (BungeeAbstractCommandContainer<CommandSender, BungeeCommandManager<CommandSender>> container : containers) {
        if (container.getClass().isAnnotationPresent(CommandContainer.class)) {
          annotationParser.parse(container);
          parseAll = false;
        }
      }
      if (parseAll) {
        annotationParser.parseContainers();
      }
    } catch (Exception e) {
      EdgyUtils.bungee().getLogger().log(Level.SEVERE, "Failed to parse command containers!");
      e.printStackTrace();
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
