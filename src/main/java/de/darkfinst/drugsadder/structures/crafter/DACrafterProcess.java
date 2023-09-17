package de.darkfinst.drugsadder.structures.crafter;

import de.darkfinst.drugsadder.recipe.DACrafterRecipe;
import de.darkfinst.drugsadder.structures.DAProcess;
import de.darkfinst.drugsadder.structures.DAStructure;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DACrafterProcess extends DAProcess {

    private DACrafterRecipe daCrafterRecipe = null;


    @Override
    public void finish(DAStructure daCrafter, boolean isAsync) {

    }

    @Override
    public void restart(DAStructure daCrafter) {

    }
}
