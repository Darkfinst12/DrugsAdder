package de.darkfinst.drugsadder.api.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class DrugsAdderLoadDataEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Type type;

    private final Object object;

    public DrugsAdderLoadDataEvent(boolean isAsync, Type type, Object object) {
        super(isAsync);
        this.type = type;
        this.object = object;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    //Required by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
    }


    public enum Type {
        WORLD, GLOBAL,
    }
}
