package de.darkfinst.drugsadder.structures.press;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
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
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public DAPressBody getBody() {
        return (DAPressBody) super.getBody();
    }

    public void usePress(Player player) {
        if (player.hasPermission("drugsadder.press.use")) {
            try {
                Block block = this.getBody().getPiston();
                Piston piston = (Piston) block.getBlockData();
                if (piston.isExtended()) {
                    piston.setExtended(true);
                    block.setBlockData(piston, false);
                    Block head = block.getRelative(piston.getFacing());
                    head.setType(Material.AIR);
                } else {
                    piston.setExtended(true);
                    block.setBlockData(piston, false);
                    Block head = block.getRelative(piston.getFacing());
                    head.setType(Material.PISTON_HEAD);
                    PistonHead headData = (PistonHead) head.getBlockData();
                    headData.setFacing(piston.getFacing());
                    head.setBlockData(headData, false);
                }
            }catch (Exception e) {
                DA.loader.unregisterDAStructure(this);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoUse"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }
}
