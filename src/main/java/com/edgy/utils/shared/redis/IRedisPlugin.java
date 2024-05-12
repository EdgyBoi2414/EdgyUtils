package com.edgy.utils.shared.redis;

import com.edgy.utils.shared.redis.models.MessageTransferObject;
import com.edgy.utils.shared.redis.models.RedisConfiguration;
import java.util.logging.Logger;

public interface IRedisPlugin {

  void runAsync(Runnable runnable);

  void onMessageReceived(String channel, MessageTransferObject message);

  Logger logger();

  default String loadRedis(String serverIdentifier, RedisConfiguration redisConfiguration) {
    // Initialize RedisManager object
    if (RedisManager.getAPI() == null) {
      RedisManager.init(this, serverIdentifier, redisConfiguration);
    }

    return serverIdentifier;
  }

}
