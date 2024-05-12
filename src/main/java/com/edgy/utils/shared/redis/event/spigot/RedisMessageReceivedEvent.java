package com.edgy.utils.shared.redis.event.spigot;

import com.edgy.utils.shared.redis.RedisManager;
import com.edgy.utils.shared.redis.event.IRedisMessageReceivedEvent;
import com.edgy.utils.shared.redis.models.MessageTransferObject;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Spigot Event class used when message was received from subscribed channel.
 */
public class RedisMessageReceivedEvent extends Event implements IRedisMessageReceivedEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Name of the channel the message came from
     */
    private final String channel;

    /**
     * MessageTransferObject containing message's data
     */
    private final MessageTransferObject messageTransferObject;

    /**
     * Constructs the instance of the Event
     *
     * @param channel   Channel in which was the message published
     * @param messageTransferObject {@link MessageTransferObject} object containing data about published message
     */
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
        return this.messageTransferObject.getSenderIdentifier().equals(RedisManager.getAPI().getServerIdentifier());
    }

    @Override
    public long getTimeStamp() {
        return this.messageTransferObject.getTimestamp();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}