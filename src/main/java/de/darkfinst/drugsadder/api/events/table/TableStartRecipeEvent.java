package de.darkfinst.drugsadder.api.events.table;

import de.darkfinst.drugsadder.recipe.DATableRecipe;
import de.darkfinst.drugsadder.structures.table.DATable;
import lombok.Getter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TableStartRecipeEvent extends TableEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final HumanEntity who;
    @Getter
    private final DATable where;
    @Getter
    private final DATableRecipe recipe;
    private boolean isCancelled = false;

    public TableStartRecipeEvent(HumanEntity who, DATable where, DATableRecipe recipe) {
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
