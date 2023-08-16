package de.darkfinst.drugsadder.structures.press;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;

public class DAPress extends DAStructure {

    public void create(Block sign, Player player) {
        if (player.hasPermission("drugsadder.press.create")) {
            DAPressBody pressBody = new DAPressBody(this, sign);
            try {
                boolean isValid = pressBody.isValidPress();
                if (isValid) {
                    super.setBody(pressBody);
                    DA.loader.registerDAStructure(this);
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_Created"));
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_Invalid"));
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoCreate"));
        }
    }

    public DAPressBody getBody() {
        return (DAPressBody) super.getBody();
    }

    public void usePress(Player player) {
        if (player.hasPermission("drugsadder.press.use")) {
            Block lever = this.getBody().getLever();
            Switch leverData = (Switch) lever.getBlockData();
            leverData.setPowered(leverData.isPowered());
            lever.setBlockData(leverData);
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoUse"));
        }
    }
}
