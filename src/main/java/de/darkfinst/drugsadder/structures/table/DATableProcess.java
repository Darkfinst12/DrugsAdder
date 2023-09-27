package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.recipe.DATableRecipe;
import de.darkfinst.drugsadder.structures.DAProcess;
import de.darkfinst.drugsadder.structures.DAStructure;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class DATableProcess extends DAProcess {

    /**
     * The recipe of the first slots
     */
    @Nullable
    private DATableRecipe recipeOne = null;
    /**
     * The recipe of the second slots
     */
    @Nullable
    private DATableRecipe recipeTwo = null;

    @Override
    public boolean isFinished() {
        return super.isFinished() && (this.getState() == 20 || this.getState() == 10 || this.getState() == 5);
    }

    @Deprecated(forRemoval = true)
    public int getSide() {
        if (this.recipeOne == null && this.recipeTwo != null && this.getState() != 9) {
            return 1;
        } else if (this.recipeOne != null && this.recipeTwo == null && this.getState() != 5) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Resets the process to the default values
     */
    @Override
    public void reset() {
        super.reset();
        this.recipeOne = null;
        this.recipeTwo = null;
    }

    /**
     * Finishes the process
     *
     * @param daTable the table to finish
     * @param isAsync if the process is async
     */
    @Override
    public void finish(DAStructure daTable, boolean isAsync) {
        if (this.recipeOne != null) {
            this.recipeOne.finishProcess((DATable) daTable, isAsync);
        } else if (this.recipeTwo != null) {
            this.recipeTwo.finishProcess((DATable) daTable, isAsync);
        }
    }

    /**
     * Restarts the process
     *
     * @param daTable the table to restart
     */
    @Override
    public void restart(DAStructure daTable) {
        if (this.getState() != 20 && this.getState() != 10 && this.getState() != 5) {
            if (this.getState() < 5 && this.recipeOne != null) {
                this.recipeOne.restartProcess((DATable) daTable, this.getState());
            } else if (this.getState() > 5 && this.getState() < 10 && this.recipeTwo != null) {
                this.recipeTwo.restartProcess((DATable) daTable, this.getState());
            }
            if (getState() > 10 && getState() < 15 && this.recipeOne != null) {
                this.recipeOne.restartProcess((DATable) daTable, this.getState());
            }
            if (getState() > 15 && getState() < 20 && this.recipeTwo != null) {
                this.recipeTwo.restartProcess((DATable) daTable, this.getState());
            }
        }
    }
}

