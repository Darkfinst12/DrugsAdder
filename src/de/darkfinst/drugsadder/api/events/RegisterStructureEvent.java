package de.darkfinst.drugsadder.api.events;

import de.darkfinst.drugsadder.structures.DAStructure;
import lombok.Getter;
import org.bukkit.block.Structure;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class RegisterStructureEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final DAStructure structure;

    private boolean isCancelled;

    public RegisterStructureEvent(boolean isAsync, DAStructure structure) {
        super(isAsync);
        this.structure = structure;
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

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
