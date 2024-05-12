package com.edgy.utils.shared.redis.event.velocity;

import com.edgy.utils.shared.redis.IRedisPlugin;
import com.edgy.utils.shared.redis.RedisManager;
import com.edgy.utils.shared.redis.event.IRedisMessageReceivedEvent;
import com.edgy.utils.shared.redis.models.MessageTransferObject;

public class RedisMessageReceivedEvent implements IRedisMessageReceivedEvent {

  private final String channel;
  private final MessageTransferObject messageTransferObject;

  public RedisMessageReceivedEvent(String channel, MessageTransferObject messageTransferObject) {
    this.channel = channel;
    this.messageTransferObject = messageTransferObject;
  }

  @Override
  public String getSenderIdentifier() {
    return this.messageTransferObject.getSenderIdentifier();
  }

  @Override
  public String getChannel() {
    return this.channel;
  }

  @Override
  public String getMessage() {
    return this.messageTransferObject.getMessage();
  }

  @Override
  public <T> T getMessageObject(Class<T> objectClass) {
    return this.messageTransferObject.parseMessageObject(objectClass);
  }

  @Override
  public boolean isSelfSender() {
    return this.messageTransferObject.getSenderIdentifier()
        .equals(RedisManager.getAPI().getServerIdentifier());
  }

  @Override
  public long getTimeStamp() {
    return this.messageTransferObject.getTimestamp();
  }
}
