package de.darkfinst.drugsadder.api.events.barrel;

import de.darkfinst.drugsadder.recipe.DABarrelRecipe;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BarrelProcessMaterialsEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    @Getter
    private final DABarrelRecipe recipe;

    public BarrelProcessMaterialsEvent(DABarrelRecipe recipe) {
        super();
        this.recipe = recipe;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
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

}
