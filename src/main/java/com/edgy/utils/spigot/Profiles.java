package com.edgy.utils.spigot;

import com.edgy.utils.geyser.Bedrock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerProfile;

public class Profiles {

  private static final List<PlayerProfile> cache = new ArrayList<>();

  public static CompletableFuture<PlayerProfile> get(OfflinePlayer player) {
    return get(player.getUniqueId());
  }

  public static CompletableFuture<PlayerProfile> get(UUID uniqueId) {
   PlayerProfile playerProfile = null;
    for (PlayerProfile profile : cache) {
      if (Objects.equals(profile.getUniqueId(), uniqueId)) {
        playerProfile = profile;
        break;
      }
    }

    if (playerProfile == null) {
      return Bukkit.createPlayerProfile(uniqueId)
          .update()
          .thenApply(updated -> {
            cache.add(updated);
            return updated;
          });
    }

    return CompletableFuture.completedFuture(playerProfile);
  }

}
