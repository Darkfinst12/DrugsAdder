package de.darkfinst.drugsadder.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreFabEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public PreFabEvent() {
    }

    //Required by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
