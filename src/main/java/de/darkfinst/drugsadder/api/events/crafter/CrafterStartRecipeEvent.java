package de.darkfinst.drugsadder.api.events.crafter;

import de.darkfinst.drugsadder.recipe.DACrafterRecipe;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import lombok.Getter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CrafterStartRecipeEvent extends CrafterEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final HumanEntity who;
    @Getter
    private final DACrafter where;
    @Getter
    private final DACrafterRecipe recipe;
    private boolean isCancelled = false;

    public CrafterStartRecipeEvent(HumanEntity who, DACrafter where, DACrafterRecipe recipe) {
        this.who = who;
        this.where = where;
        this.recipe = recipe;
    }

    //Required by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
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
}
