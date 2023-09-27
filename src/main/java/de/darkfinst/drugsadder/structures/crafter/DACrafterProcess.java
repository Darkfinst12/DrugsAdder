package de.darkfinst.drugsadder.structures.crafter;

import de.darkfinst.drugsadder.recipe.DACrafterRecipe;
import de.darkfinst.drugsadder.structures.DAProcess;
import de.darkfinst.drugsadder.structures.DAStructure;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DACrafterProcess extends DAProcess {

    /**
     * The recipe of the process
     */
    private DACrafterRecipe recipe = null;

    @Override
    public void finish(DAStructure daCrafter, boolean isAsync) {
        if (this.recipe != null) {
            this.recipe.finishProcess((DACrafter) daCrafter, isAsync);
        }
    }

    @Override
    public void reset() {
        super.reset();
        recipe = null;
    }

    @Override
    public void restart(DAStructure daStructure) {
        //Do nothing - not needed
    }
}
