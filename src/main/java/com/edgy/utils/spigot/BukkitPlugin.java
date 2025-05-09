package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.spigot.CloudCommands.AbstractCommandContainer;
import com.edgy.utils.shared.Manager;
import com.edgy.utils.shared.Messages;
import com.edgy.utils.spigot.listener.OnlinePlayerCollectionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.Source;

public abstract class BukkitPlugin extends JavaPlugin {

  private final List<Manager> managers = new ArrayList<>();
  private Messages<CommandSender> messages;

  @Override
  public final void onEnable() {
    EdgyUtils.initializeBukkit(this);

    try {
      messages = setupMessages();
      managers.addAll(setupManagers());
      CloudCommands.bukkit(setupCommands());
      for (Listener listener : setupListeners()) {
        getServer().getPluginManager().registerEvents(listener, this);
      }

      getServer().getPluginManager().registerEvents(new OnlinePlayerCollectionListener(), this);
    } catch (Exception err) {
      err.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    Tasks.clearAsyncs();
  }

  public final int reload() {
    int fails = 0;
    for (Manager manager : managers) {
      try {
        manager.reload();
      } catch (Exception err) {
        manager.log(Level.SEVERE, "Error while reloading", err);
        fails++;
      }
    }

    messages.reload(getDataFolder(), messages.getMessagesFile(), provideClassLoader());

    return fails;
  }

  @SuppressWarnings("unchecked")
  public final <M extends Manager> M manager(Class<M> clazz) {
    for (Manager manager : managers) {
      if (manager.getClass().equals(clazz)) {
        return (M) manager;
      }
    }
    throw new IllegalArgumentException("Manager not found: " + clazz.getSimpleName());
  }

  public final Messages<CommandSender> messages() {
    return messages;
  }


  protected List<AbstractCommandContainer<Source, PaperCommandManager<Source>>> setupCommands() {
    return new ArrayList<>();
  }

  protected abstract Messages<CommandSender> setupMessages();

  protected abstract List<Manager> setupManagers();
  protected abstract List<Listener> setupListeners();
  protected abstract ClassLoader provideClassLoader();

}
