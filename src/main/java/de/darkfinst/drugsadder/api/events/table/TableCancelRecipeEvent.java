package de.darkfinst.drugsadder.api.events.table;

import de.darkfinst.drugsadder.recipe.DATableRecipe;
import de.darkfinst.drugsadder.structures.table.DATable;
import lombok.Getter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class TableCancelRecipeEvent extends TableEvent {

    private static final HandlerList handlers = new HandlerList();

    private final HumanEntity who;
    private final DATable where;
    private final DATableRecipe recipe;

    public TableCancelRecipeEvent(HumanEntity who, DATable where, DATableRecipe recipe) {
        this.who = who;
        this.where = where;
        this.recipe = recipe;
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
