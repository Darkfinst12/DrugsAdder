package de.darkfinst.drugsadder.api.events;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class DrugsAdderSendMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final @NotNull CommandSender sender;
    private final Type type;
    private @NotNull Component message;
    private boolean cancelled;


    public DrugsAdderSendMessageEvent(boolean isAsync, @NotNull CommandSender sender, @NotNull Component message, Type type) {
        super(isAsync);
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.cancelled = false;
    }

    // Required by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setMessage(@NotNull Component component) {
        this.message = component;
    }

    public void setMessage(@NotNull String message) {
        this.message = Component.text(message);
    }

    public @NotNull String getCleanMessage() {
        return PlainTextComponentSerializer.plainText().serialize(this.message);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum Type {
        DEBUG,
        INFO,
        ERROR,
        LOG,
        NONE,
        PERMISSION,
        PLAYER,
        COMMAND,
    }
}
