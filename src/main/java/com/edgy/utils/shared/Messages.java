package com.edgy.utils.shared;

import com.edgy.utils.EdgyUtils;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public class Messages<S> {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
  private static final String[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".split("");
  private static final String[] SMALL_CAPS_ALPHABET = "\u1D00\u0299\u1D04\u1D05\u1D07\uA730\u0262\u029C\u026A\u1D0A\u1D0B\u029F\u1D0D\u0274\u1D0F\u1D18q\u0280\uA731\u1D1B\u1D1C\u1D20\u1D21x\u028F\u1D22".split("");
  private final Function<S, Audience> audienceProvider;
  private ResourceBundle MESSAGES;
  private final boolean legacy;
  private final String messagesFile;

  public Messages(Function<S, Audience> audienceProvider, File dataFolder, String messagesFile, ClassLoader classLoader) {
    this(audienceProvider, dataFolder, messagesFile, classLoader, false);
  }

  public Messages(
      Function<S, Audience> audienceProvider,
      File dataFolder,
      String messagesFile,
      ClassLoader classLoader,
      boolean legacy
  ) {
    this.audienceProvider = audienceProvider;
    this.messagesFile = messagesFile;
    this.legacy = legacy;

    reload(dataFolder, messagesFile, classLoader);
  }

  public String getMessagesFile() {
    return messagesFile;
  }

  public void reload(File dataFolder, String messagesFile, ClassLoader classLoader) {
    ResourceUtils.saveResource(dataFolder, messagesFile, classLoader);
    try (InputStream stream = Files.newInputStream(
        new File(dataFolder, messagesFile).toPath())) {
      MESSAGES = new PropertyResourceBundle(stream);
    } catch (Exception err) {
      Bukkit.getLogger().severe("Failed to load messages.properties!");
      MESSAGES = ResourceBundle.getBundle("messages");
    }
  }

  public void sendMessage(Collection<? extends S> senders, String key) {
    sendMessage(senders, key, new HashMap<>());
  }

  public void sendMessage(Collection<? extends S> senders, String key, Sound sound) {
    sendMessage(senders, key, new HashMap<>(), sound);
  }

  public void sendMessage(Collection<? extends S> senders, String key, Map<String, Object> args) {
    sendMessage(senders, key, args, null);
  }

  public void sendMessage(Collection<? extends S> senders, String key, Map<String, Object> args,
      @Nullable Sound sound) {
    for (S sender : senders) {
      sendMessage(sender, key, args, sound);
    }
  }

  public void sendMessage(S sender, String key) {
    sendMessage(sender, key, new HashMap<>(), null);
  }

  public void sendMessage(S sender, String key, @Nullable Sound sound) {
    sendMessage(sender, key, new HashMap<>(), sound);
  }

  public void sendMessage(S sender, String key, Map<String, Object> args) {
    sendMessage(sender, key, args, null);
  }

  public void sendMessage(S sender, String key, Map<String, Object> args, @Nullable Sound sound) {
    Audience audience = audienceProvider.apply(sender);
    audience.sendMessage(component(key, args));
    if (sound != null) {
      audience.playSound(sound);
    }
  }

  public List<String> stringList(String... lines) {
    return Arrays.stream(lines)
        .map(line -> LegacyComponentSerializer.legacySection().serialize(component(line)))
        .collect(Collectors.toList());
  }

  public List<String> stringList(String key) {
    return stringList(key, MapBuilder.mapOf());
  }

  public List<String> stringList(String key, Map<String, Object> args) {
    return componentList(key, args)
        .stream()
        .map(LegacyComponentSerializer.legacySection()::serialize)
        .collect(Collectors.toList());
  }

  public List<Component> componentList(String key) {
    return componentList(key, MapBuilder.mapOf());
  }

  public List<Component> componentList(String key, Map<String, Object> args) {
    String message;
    try {
      message = MESSAGES.getString(key);
    } catch (Exception err) {
      message = key;
    }

    for (String arg : args.keySet()) {
      message = message.replace("{" + arg + "}", args.get(arg).toString());
    }

    return Arrays.stream(message.split("<n>"))
        .map(this::component)
        .collect(Collectors.toList());
  }

  public List<Component> componentListFromStrings(String... lines) {
    return Arrays.stream(lines)
        .map(this::component)
        .collect(Collectors.toList());
  }

  public String string(String key) {
    return string(key, MapBuilder.mapOf());
  }

  public String string(String key, Map<String, Object> args) {
    return LegacyComponentSerializer.legacySection().serialize(component(key, args));
  }

  public String string(String key, OfflinePlayer player) {
    return string(key, new HashMap<>(), player);
  }

  public String string(String key, Map<String, Object> args, OfflinePlayer player) {
    return LegacyComponentSerializer.legacySection().serialize(component(key, args, player));
  }

  public Component component(String key) {
    return component(key, new HashMap<>());
  }

  public Component component(String key, OfflinePlayer offlinePlayer) {
    return component(key, new HashMap<>(), offlinePlayer);
  }

  public Component component(String key, Map<String, Object> args) {
    return component(key, args, null);
  }

  public Component component(String key, Map<String, Object> args, OfflinePlayer player) {
    String message;
    try {
      message = MESSAGES.getString(key);
    } catch (Exception err) {
      message = key;
    }

    for (String arg : args.keySet()) {
      message = message.replace("{" + arg + "}", args.get(arg).toString());
    }

    if (EdgyUtils.papi()) {
      message = PlaceholderAPI.setPlaceholders(player, message);
    }

    if (legacy) {
      return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    return MINI_MESSAGE.deserialize(
        message,
        TagResolver.resolver("sm_caps", (argumentQueue, context) -> {
              final String text = argumentQueue.popOr("No text provided").value();
              return Tag.inserting(context.deserialize(toSmallCaps(text)));
            }
        )
    );
  }

  public void playSound(S sender, Sound sound) {
    audienceProvider.apply(sender).playSound(sound);
  }

  public void title(S sender, String title, String subtitle, Map<String, Object> args, int fadeIn,
      int stay, int fadeOut) {
    title(Collections.singletonList(sender), title, subtitle, args, fadeIn, stay, fadeOut);
  }

  public void title(Collection<? extends S> senders, String title, String subtitle,
      Map<String, Object> args, int fadeIn,
      int stay, int fadeOut) {
    Title kyoriTitle = Title.title(
        component(title, args),
        component(subtitle, args),
        Title.Times.times(
            Duration.of(fadeIn, ChronoUnit.SECONDS),
            Duration.of(stay, ChronoUnit.SECONDS),
            Duration.of(fadeOut, ChronoUnit.SECONDS)
        )
    );

    for (S sender : senders) {
      title(sender, kyoriTitle);
    }
  }

  private void title(S sender, Title title) {
    audienceProvider.apply(sender).showTitle(title);
  }

  public static String toSmallCaps(String text) {
    text = text.toLowerCase();
    StringBuilder convertedBuilder = new StringBuilder();
    boolean ignoringNextChar = false;
    for (char textCharacter : text.toCharArray()) {
      int index = 0;
      boolean successfullyTranslated = false;
      if (textCharacter == '&' || textCharacter == '\u00A7') {
        ignoringNextChar = true;
        convertedBuilder.append(textCharacter);
        continue;
      }
      if (ignoringNextChar) {
        ignoringNextChar = false;
        convertedBuilder.append(textCharacter);

        continue;
      }
      for (String alphabetLetter : ALPHABET) {
        if ((textCharacter + "").equals(alphabetLetter)) {
          convertedBuilder.append(SMALL_CAPS_ALPHABET[index]);
          successfullyTranslated = true;
          break;
        }

        index++;
      }

      if (!successfullyTranslated) {
        convertedBuilder.append(textCharacter);
      }
    }

    return convertedBuilder.toString();
  }

  public Audience audience(S sender) {
    return audienceProvider.apply(sender);
  }

  public Audience audience(Collection<? extends S> senders) {
    return Audience.audience(senders.stream().map(audienceProvider).collect(Collectors.toList()));
  }

}
