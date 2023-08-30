package de.darkfinst.drugsadder.api.events.press;

import de.darkfinst.drugsadder.recipe.DAPressRecipe;
import de.darkfinst.drugsadder.structures.press.DAPress;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PressItemEvent extends PressEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    @Getter
    private final DAPressRecipe recipe;

    public PressItemEvent(DAPress press, DAPressRecipe recipe) {
        super(press);
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
