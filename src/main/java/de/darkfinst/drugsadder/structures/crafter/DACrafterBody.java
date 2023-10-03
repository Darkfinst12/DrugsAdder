package de.darkfinst.drugsadder.structures.crafter;

import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.structures.DABody;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;

@Getter
public class DACrafterBody extends DABody {


    /**
     * The process of the crafter
     */
    private final DACrafterProcess process = new DACrafterProcess();

    public DACrafterBody(Block sign) {
        super(sign.getWorld(), sign);
    }

    /**
     * Checks if the crafter is valid
     *
     * @return true if the crafter is valid
     * @throws ValidateStructureException if the crafter is not valid
     */
    public boolean isValidCrafter() throws ValidateStructureException {
        this.blocks.add(this.sign);

        WallSign wallSign = (WallSign) this.sign.getBlockData();
        BlockFace face = wallSign.getFacing();

        Block craftingBSign = this.sign.getRelative(face.getOppositeFace());
        boolean isValid = Material.CRAFTING_TABLE.equals(craftingBSign.getType());
        if (!isValid) {
            throw new ValidateStructureException("CraftingTableBehindSign is not valid!");
        }
        this.blocks.add(craftingBSign);

        return true;
    }
}
