package de.darkfinst.drugsadder.api.events.drug;

import de.darkfinst.drugsadder.DADrug;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DrugReduceAddictionEvent extends DrugEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final int oldAddiction;
    private boolean isCancelled = false;
    @Getter
    @Setter
    private int newAddiction;

    public DrugReduceAddictionEvent(DADrug drug, int oldAddiction, int newAddiction, boolean isAsync) {
        super(isAsync, drug);
        this.oldAddiction = oldAddiction;
        this.newAddiction = newAddiction;
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
