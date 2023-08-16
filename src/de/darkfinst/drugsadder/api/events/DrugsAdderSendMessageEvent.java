package de.darkfinst.drugsadder.api.events;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class DrugsAdderSendMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final CommandSender sender;
    private String message;
    private boolean cancelled;


    public DrugsAdderSendMessageEvent(boolean isAsync, CommandSender sender, String message) {
        super(isAsync);
        this.sender = sender;
        this.message = message;
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
}
