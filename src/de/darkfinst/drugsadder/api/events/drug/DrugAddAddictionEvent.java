package de.darkfinst.drugsadder.api.events.drug;

import de.darkfinst.drugsadder.DADrug;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DrugAddAddictionEvent extends DrugEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean isCancelled = false;
    @Getter
    private final int oldAddiction;
    @Getter
    @Setter
    private int newAddiction;

    public DrugAddAddictionEvent(DADrug drug, int oldAddiction, int newAddiction) {
        super(drug);
        this.oldAddiction = oldAddiction;
        this.newAddiction = newAddiction;
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
