package de.darkfinst.drugsadder.api.events;

import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class DrugsAdderSendMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final CommandSender sender;
    private TextComponent message;
    private boolean cancelled;
    private final Type type;


    public DrugsAdderSendMessageEvent(boolean isAsync, CommandSender sender, String message, Type type) {
        super(isAsync);
        this.sender = sender;
        this.message = new TextComponent(message);
        this.type = type;
        this.cancelled = false;
    }

    public DrugsAdderSendMessageEvent(boolean isAsync, CommandSender sender, TextComponent message, Type type) {
        super(isAsync);
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setMessage(String message) {
        this.message = new TextComponent(message);
    }

    public void setMessage(TextComponent message) {
        this.message = message;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Required by Bukkit
    public static HandlerList getHandlerList() {
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
