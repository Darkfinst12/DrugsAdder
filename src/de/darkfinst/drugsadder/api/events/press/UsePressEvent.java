package de.darkfinst.drugsadder.api.events.press;

import de.darkfinst.drugsadder.structures.press.DAPress;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UsePressEvent extends PressEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    @Getter
    private final Player player;

    public UsePressEvent(DAPress press, Player player) {
        super(press);
        this.player = player;
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
