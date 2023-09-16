package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.recipe.DATableRecipe;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class DATableProcess {


    @Nullable
    private DATableRecipe recipeOne;
    @Nullable
    private DATableRecipe recipeTwo;

    private int state = 0;
    private int taskID = -1;

    public boolean isFinished() {
        return this.taskID == -1 && (this.state == 20 || this.state == 10 || this.state == 5);
    }

    public boolean isProcessing() {
        return this.taskID != -1;
    }

    public int getSide() {
        if (this.recipeOne == null && this.recipeTwo != null && this.state != 9) {
            return 1;
        } else if (this.recipeOne != null && this.recipeTwo == null && this.state != 5) {
            return 0;
        } else {
            return -1;
        }
    }

    public void reset() {
        this.recipeOne = null;
        this.recipeTwo = null;
        this.state = 0;
        this.taskID = -1;
    }

    public void finish(DATable daTable) {
        if (this.recipeOne != null) {
            this.recipeOne.finishProcess(daTable);
        } else if (this.recipeTwo != null) {
            this.recipeTwo.finishProcess(daTable);
        }
    }
}
