package com.edgy.utils.spigot.ui.element;

import java.util.Objects;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.incendo.interfaces.core.click.ClickContext;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.pane.ChestPane;

public final class GUIElement {

    private final ItemStack item;
    private final ClickHandler<ChestPane, InventoryClickEvent, PlayerViewer, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>> clickHandler;
    public GUIElement(
        ItemStack item,
        ClickHandler<ChestPane, InventoryClickEvent, PlayerViewer, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>> clickHandler
    ) {
      this.item = item;
      this.clickHandler = clickHandler;
    }

    public ClickHandler<ChestPane, InventoryClickEvent, PlayerViewer, ClickContext<ChestPane, InventoryClickEvent, PlayerViewer>> clickHandler() {
      if (clickHandler == null) {
        return (context) -> {
        };
      }

      return clickHandler;
    }

    public ItemStack item() {
      return item;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null || obj.getClass() != this.getClass()) {
        return false;
      }
      var that = (GUIElement) obj;
      return Objects.equals(this.item, that.item) &&
          Objects.equals(this.clickHandler, that.clickHandler);
    }

    @Override
    public int hashCode() {
      return Objects.hash(item, clickHandler);
    }

    @Override
    public String toString() {
      return "GUIElement[" +
          "item=" + item + ", " +
          "consumer=" + clickHandler + ']';
    }

  }