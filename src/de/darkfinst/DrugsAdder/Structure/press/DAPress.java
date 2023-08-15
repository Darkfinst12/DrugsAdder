package de.darkfinst.DrugsAdder.Structure.press;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.Structure.DAStructure;
import de.darkfinst.DrugsAdder.exceptions.ValidateStructureException;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;

public class DAPress extends DAStructure {

    public boolean create(Block sign, Player player) {
        DAPressBody pressBody = new DAPressBody(this, sign);
        boolean isValid = false;
        try {
            isValid = pressBody.isValidPress();
            if (isValid) {
                super.setBody(pressBody);
                DA.loader.registerDAStructure(this);
            }
        } catch (ValidateStructureException ignored) {
        }
        return isValid;
    }

    public DAPressBody getBody() {
        return (DAPressBody) super.getBody();
    }

    public void usePress() {
        Block lever = this.getBody().getLever();
        Switch leverData = (Switch) lever.getBlockData();
        leverData.setPowered(leverData.isPowered());
        lever.setBlockData(leverData);
    }
}
