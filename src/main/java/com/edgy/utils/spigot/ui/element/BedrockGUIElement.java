package com.edgy.utils.spigot.ui.element;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.util.FormImage;

public final class BedrockGUIElement {

    private final String name;
    private final FormImage formImage;
    private final BiConsumer<Player, Map<String, Object>> consumer;

    public BedrockGUIElement(String name, FormImage formImage,
        BiConsumer<Player, Map<String, Object>> consumer) {
      this.name = name;
      this.formImage = formImage;
      this.consumer = consumer;
    }

    public String name() {
      return name;
    }

    public FormImage formImage() {
      return formImage;
    }

    public BiConsumer<Player, Map<String, Object>> consumer() {
      return consumer;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null || obj.getClass() != this.getClass()) {
        return false;
      }
      var that = (BedrockGUIElement) obj;
      return Objects.equals(this.name, that.name) &&
          Objects.equals(this.formImage, that.formImage) &&
          Objects.equals(this.consumer, that.consumer);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, formImage, consumer);
    }

    @Override
    public String toString() {
      return "BedrockGUIElement[" +
          "name=" + name + ", " +
          "formImage=" + formImage + ", " +
          "consumer=" + consumer + ']';
    }


  }