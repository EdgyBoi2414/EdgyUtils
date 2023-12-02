package com.edgy.utils.shared;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.edgy.utils.EdgyUtils;
import com.edgy.utils.spigot.Paper;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CloudCommands {

  private final static AbstractCommandContainer<CommandSender, CommandManager<CommandSender>> BUKKIT_DEBUG_CONTAINER = new AbstractCommandContainer<>() {
    @Override
    protected void registerCommands(CommandManager<CommandSender> commandManager) {
      commandManager.command(
          commandManager.commandBuilder("edgyutils", "eu")
              .literal("debug")
              .senderType(CommandSender.class)
              .handler(commandContext -> {
                DebugLogger.enabled(!DebugLogger.enabled());
                commandContext.getSender().sendMessage("Debug mode is now " + (DebugLogger.enabled() ? "enabled" : "disabled"));
              })
      );
    }
  };

  @SafeVarargs
  public static void bukkit(
      AbstractCommandContainer<CommandSender, BukkitCommandManager<CommandSender>>... container
  ) {
    bukkit(Arrays.asList(container));
  }

  public static void bukkit(
      List<AbstractCommandContainer<CommandSender, BukkitCommandManager<CommandSender>>> containers
  ) {
    final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
        AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();
    final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
    final BukkitCommandManager<CommandSender> commandManager;
    final AnnotationParser<CommandSender> annotationParser;
    final BukkitAudiences bukkitAudiences = BukkitAudiences.create(EdgyUtils.bukkit());

    try {
      if (Paper.isPaper()) {
        commandManager = new PaperCommandManager<>(
            EdgyUtils.bukkit(),
            executionCoordinatorFunction,
            mapperFunction,
            mapperFunction
        );
        ((PaperCommandManager<CommandSender>) commandManager).registerAsynchronousCompletions();
      } else {
        commandManager = new BukkitCommandManager<>(
            EdgyUtils.bukkit(),
            executionCoordinatorFunction,
            mapperFunction,
            mapperFunction
        );
      }
    } catch (Exception e) {
      Bukkit.getLogger().log(Level.SEVERE, "Failed to initialize command manager!");
      e.printStackTrace();
      return;
    }

    AudienceProvider<CommandSender> audience = new AudienceProvider<CommandSender>() {
      @Override
      public @NonNull Audience apply(@NonNull CommandSender sender) {
        return bukkitAudiences.sender(sender);
      }
    };

    final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
        CommandMeta.simple()
            .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
            .build();
    annotationParser = new AnnotationParser<>(
        commandManager,
        CommandSender.class,
        commandMetaFunction
    );

    new MinecraftExceptionHandler<CommandSender>()
        .withInvalidSyntaxHandler()
        .withInvalidSenderHandler()
        .withNoPermissionHandler()
        .withArgumentParsingHandler()
        .withCommandExecutionHandler()
        .apply(commandManager, audience);

    try {
      annotationParser.parseContainers();
    } catch (Exception e) {
      Bukkit.getLogger().log(Level.SEVERE, "Failed to parse command containers!");
      e.printStackTrace();
    }
    for (AbstractCommandContainer<CommandSender, BukkitCommandManager<CommandSender>> container : containers) {
      container.registerCommands(commandManager);
    }

    BUKKIT_DEBUG_CONTAINER.registerCommands(commandManager);
  }

  public static abstract class AbstractCommandContainer<S, C extends CommandManager<S>> {

    protected abstract void registerCommands(C commandManager);

  }

}
