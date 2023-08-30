package de.darkfinst.drugsadder.api.events.press;

import de.darkfinst.drugsadder.structures.press.DAPress;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompressItemsEvent extends PressEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    @Getter
    @Setter
    private List<Item> items;

    public CompressItemsEvent(DAPress press, List<Item> items) {
        super(press);
        this.items = items;
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
