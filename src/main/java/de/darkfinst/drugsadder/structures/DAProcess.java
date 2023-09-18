package de.darkfinst.drugsadder.structures;

import de.darkfinst.drugsadder.structures.table.DATable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DAProcess {

    private int state = 0;
    private int taskID = -1;

    public boolean isFinished() {
        return this.taskID == -1;
    }

    public boolean isProcessing() {
        return this.taskID != -1;
    }

    public void reset() {
        this.state = 0;
        this.taskID = -1;
    }

    public abstract void finish(DAStructure daStructure, boolean isAsync);

    public abstract void restart(DAStructure daStructure);
}
