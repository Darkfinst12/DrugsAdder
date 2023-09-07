package de.darkfinst.drugsadder.structures.plant;

import de.darkfinst.drugsadder.structures.DABody;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;

@Getter
public class DAPlantBody extends DABody {

    private final DAPlant daPlant;
    private final Block plantBLock;

    public DAPlantBody(DAPlant daPlant, Block plantBLock) {
        super(plantBLock.getWorld());
        this.daPlant = daPlant;
        this.plantBLock = plantBLock;
    }

    public boolean isValidPlant() {
        this.blocks.add(this.plantBLock);
        boolean isValid = false;
        Material plantType = this.plantBLock.getType();
        if (Tag.CORAL_PLANTS.isTagged(plantType) || Tag.WALL_CORALS.isTagged(plantType) || Tag.SMALL_FLOWERS.isTagged(plantType) || Tag.FLOWERS.isTagged(plantType) || Tag.SAPLINGS.isTagged(plantType) || Tag.CROPS.isTagged(plantType)) {
            isValid = true;
        } else if (Tag.TALL_FLOWERS.isTagged(plantType)) {
            Block rel = this.plantBLock.getRelative(0, 1, 0);
            isValid = rel.getType().equals(plantType);
            super.blocks.add(rel);
        }
        return isValid;
    }

}
