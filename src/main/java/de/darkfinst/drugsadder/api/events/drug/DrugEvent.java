package de.darkfinst.drugsadder.api.events.drug;

import de.darkfinst.drugsadder.DADrug;
import org.bukkit.event.Event;

public abstract class DrugEvent extends Event {
    private final DADrug drug;

    public DrugEvent(DADrug drug) {
        this.drug = drug;
    }

    public DrugEvent(boolean isAsync, DADrug drug) {
        super(isAsync);
        this.drug = drug;
    }
}
