package de.darkfinst.drugsadder.api.events.drug;

import de.darkfinst.drugsadder.DADrug;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DrugConsumeEvent extends DrugEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean isCancelled = false;

    public DrugConsumeEvent(DADrug drug) {
        super(drug);
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

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
