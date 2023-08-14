package de.darkfinst.DrugsAdder.constructs.barrel;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.util.Vector;

@Getter
public class DABarrelBody {

    private final DABarrel barrel;
    private final Block sign;

    public DABarrelBody(DABarrel barrel, Block sign) {
        this.barrel = barrel;
        this.sign = sign;
    }

    public boolean isValidBarrel() {
        Bukkit.getLogger().info("--------------- Validate Barrel ----------------");
        boolean isValid = false;
        Location loc = this.sign.getLocation();
        Bukkit.getLogger().info(String.format("Location: %s", loc));
        World world = this.sign.getWorld();
        Bukkit.getLogger().info(String.format("World: %s", world.getName()));

        isValid = Material.AIR.equals(world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).getType());
        Bukkit.getLogger().info(String.format("OverSignValid: %s", isValid));
        WallSign wallSign = (WallSign) this.sign.getState().getBlockData();
        Block attachedBlock = this.sign.getRelative(wallSign.getFacing().getOppositeFace());
        isValid = Material.BARREL.equals(world.getBlockAt(attachedBlock.getLocation()).getType());
        Bukkit.getLogger().info(String.format("BehindSignValid: %s", isValid));
        isValid = Material.BARREL.equals(world.getBlockAt(attachedBlock.getLocation().getBlockX(), attachedBlock.getLocation().getBlockY() + 1, attachedBlock.getLocation().getBlockZ()).getType());
        Bukkit.getLogger().info(String.format("BehindSignValid+y1: %s", isValid));
        isValid = Material.SPRUCE_TRAPDOOR.equals(world.getBlockAt(attachedBlock.getLocation().getBlockX(), attachedBlock.getLocation().getBlockY() + 2, attachedBlock.getLocation().getBlockZ()).getType());
        Bukkit.getLogger().info(String.format("BehindSignValid+y2: %s", isValid));

        Vector direction = loc.subtract(attachedBlock.getLocation()).toVector();
        Bukkit.getLogger().info(String.format("Direction: %s", direction));
        if (direction.getX() != 0) {
            if (direction.getX() == 1) {
                //WEST
                isValid = Material.SPRUCE_TRAPDOOR.equals(world.getBlockAt(attachedBlock.getLocation().getBlockX() + 1, attachedBlock.getLocation().getBlockY(), attachedBlock.getLocation().getBlockZ()).getType());
                Bukkit.getLogger().info(String.format("BehindSignValid+WX1: %s", isValid));
                isValid = Material.SPRUCE_TRAPDOOR.equals(world.getBlockAt(attachedBlock.getLocation().getBlockX() + 1, attachedBlock.getLocation().getBlockY() + 1, attachedBlock.getLocation().getBlockZ()).getType());
                Bukkit.getLogger().info(String.format("BehindSignValid+WX2: %s", isValid));
            }
            if (direction.getX() == -1) {
                //EAST
                isValid = Material.SPRUCE_TRAPDOOR.equals(world.getBlockAt(attachedBlock.getLocation().getBlockX() - 1, attachedBlock.getLocation().getBlockY(), attachedBlock.getLocation().getBlockZ()).getType());
                Bukkit.getLogger().info(String.format("BehindSignValid+EX1: %s", isValid));
                isValid = Material.SPRUCE_TRAPDOOR.equals(world.getBlockAt(attachedBlock.getLocation().getBlockX() - 1, attachedBlock.getLocation().getBlockY() + 1, attachedBlock.getLocation().getBlockZ()).getType());
                Bukkit.getLogger().info(String.format("BehindSignValid+EX2: %s", isValid));
            }
        } else if (direction.getZ() != 0) {
            if (direction.getZ() == 1) {
                //NORTH
            }
            if (direction.getZ() == -1) {
                //SOUTH
            }
        }

        Bukkit.getLogger().info("------------------------------------------------");

        return isValid;
    }

}
