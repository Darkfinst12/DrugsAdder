package de.darkfinst.drugsadder.api.events;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class DrugsAdderSendMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final CommandSender sender;
    private final Type type;
    @Nullable
    private BaseComponent component;
    @Nullable
    private String message;
    private boolean cancelled;


    public DrugsAdderSendMessageEvent(boolean isAsync, CommandSender sender, @NotNull String message, Type type) {
        super(isAsync);
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.cancelled = false;
    }

    public DrugsAdderSendMessageEvent(boolean isAsync, CommandSender sender, @NotNull BaseComponent component, Type type) {
        super(isAsync);
        this.sender = sender;
        this.component = component;
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

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    public void setMessage(BaseComponent component) {
        this.component = component;
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
