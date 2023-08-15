package de.darkfinst.DrugsAdder.Structure.barrel;

import de.darkfinst.DrugsAdder.Structure.DABody;
import de.darkfinst.DrugsAdder.exceptions.ValidateStructureException;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.util.Vector;

@Getter
public class DABarrelBody extends DABody {

    private final DABarrel barrel;
    private final Block sign;

    public DABarrelBody(DABarrel barrel, Block sign) {
        this.barrel = barrel;
        this.sign = sign;
    }

    public boolean isValidBarrel() throws ValidateStructureException {
        this.blocks.add(this.sign);
        Location loc = this.sign.getLocation();
        World world = this.sign.getWorld();

        Block blockOSign = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
        boolean isValid = Material.AIR.equals(blockOSign.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockOverSign is not valid!");
        }
        this.blocks.add(blockOSign);

        WallSign wallSign = (WallSign) this.sign.getState().getBlockData();
        Block barrelBSign = this.sign.getRelative(wallSign.getFacing().getOppositeFace());
        isValid = Material.BARREL.equals(barrelBSign.getType());
        if (!isValid) {
            throw new ValidateStructureException("BarrelBehindSign is not valid!");
        }
        this.blocks.add(barrelBSign);

        Block barrelBSignYa1 = world.getBlockAt(barrelBSign.getLocation().getBlockX(), barrelBSign.getLocation().getBlockY() + 1, barrelBSign.getLocation().getBlockZ());
        isValid = Material.BARREL.equals(barrelBSignYa1.getType());
        if (!isValid) {
            throw new ValidateStructureException("BarrelBehindSign +1y is not valid!");
        }
        this.blocks.add(barrelBSignYa1);

        Block blockBSignYa2 = world.getBlockAt(barrelBSign.getLocation().getBlockX(), barrelBSign.getLocation().getBlockY() + 2, barrelBSign.getLocation().getBlockZ());
        isValid = Material.SPRUCE_TRAPDOOR.equals(blockBSignYa2.getType());
        if (!(isValid && blockBSignYa2.getBlockData() instanceof TrapDoor trapDoor && trapDoor.getHalf().equals(Bisected.Half.BOTTOM) && !trapDoor.isOpen())) {
            throw new ValidateStructureException("BlockBehindSign +2y is not valid!");
        }
        this.blocks.add(blockBSignYa2);

        Vector direction = loc.subtract(barrelBSign.getLocation()).toVector();
        if (direction.getX() != 0) //East and West Placement
        {
            this.checkBarrelBlocks(barrelBSign, direction, 0, 1);

        } else if (direction.getZ() != 0) //North and South Placement
        {
            this.checkBarrelBlocks(barrelBSign, direction, 1, 0);
        }

        return true;
    }

    private void checkBarrelBlocks(Block barrelBSign, Vector direction, int addX, int addZ) throws ValidateStructureException {
        World world = this.sign.getWorld();
        Block trapBBarrel = barrelBSign.getRelative((direction.getBlockX() * -1), 0, (direction.getBlockZ() * -1));
        boolean isValid = Material.SPRUCE_TRAPDOOR.equals(trapBBarrel.getType());
        if (!(isValid && trapBBarrel.getBlockData() instanceof TrapDoor trapDBBarrel && trapDBBarrel.getHalf().equals(Bisected.Half.BOTTOM) && trapDBBarrel.isOpen())) {
            throw new ValidateStructureException("TrapDoorBehindBarrel is not valid!");
        }
        this.blocks.add(trapBBarrel);

        //left
        Block trapLBarrel = world.getBlockAt(barrelBSign.getLocation().getBlockX() + addX, barrelBSign.getLocation().getBlockY(), barrelBSign.getLocation().getBlockZ() + addZ);
        isValid = Material.SPRUCE_TRAPDOOR.equals(trapLBarrel.getType());
        if (!(isValid && trapLBarrel.getBlockData() instanceof TrapDoor trapDLBarrel && trapDLBarrel.getHalf().equals(Bisected.Half.BOTTOM) && trapDLBarrel.isOpen())) {
            throw new ValidateStructureException("TrapDoorLeftOfBarrel is not valid!");
        }
        this.blocks.add(trapLBarrel);

        //right
        Block trapRBarrel = world.getBlockAt(barrelBSign.getLocation().getBlockX() - addX, barrelBSign.getLocation().getBlockY(), barrelBSign.getLocation().getBlockZ() - addZ);
        isValid = Material.SPRUCE_TRAPDOOR.equals(trapRBarrel.getType());
        if (!(isValid && trapRBarrel.getBlockData() instanceof TrapDoor trapDRBarrel && trapDRBarrel.getHalf().equals(Bisected.Half.BOTTOM) && trapDRBarrel.isOpen())) {
            throw new ValidateStructureException("TrapDoorRightOfBarrel is not valid!");
        }
        this.blocks.add(trapRBarrel);

        Block trapBBarrelYa1 = barrelBSign.getRelative((direction.getBlockX() * -1), direction.getBlockY() + 1, (direction.getBlockZ() * -1));
        isValid = Material.SPRUCE_TRAPDOOR.equals(trapBBarrelYa1.getType());
        if (!(isValid && trapBBarrelYa1.getBlockData() instanceof TrapDoor trapDBBarrelYa1 && trapDBBarrelYa1.getHalf().equals(Bisected.Half.BOTTOM) && trapDBBarrelYa1.isOpen())) {
            throw new ValidateStructureException("TrapDoorBehindBarrel +1y is not valid!");
        }
        this.blocks.add(trapBBarrelYa1);

        //Left
        Block trapLBarrelYa1 = world.getBlockAt(barrelBSign.getLocation().getBlockX() + addX, barrelBSign.getLocation().getBlockY() + 1, barrelBSign.getLocation().getBlockZ() + addZ);
        isValid = Material.SPRUCE_TRAPDOOR.equals(trapLBarrelYa1.getType());
        if (!(isValid && trapLBarrelYa1.getBlockData() instanceof TrapDoor trapDLBarrelYa1 && trapDLBarrelYa1.getHalf().equals(Bisected.Half.BOTTOM) && trapDLBarrelYa1.isOpen())) {
            throw new ValidateStructureException("TrapDoorLeftOfBarrel +1y is not valid!");
        }
        this.blocks.add(trapLBarrelYa1);

        //Right
        Block trapRBarrelYa1 = world.getBlockAt(barrelBSign.getLocation().getBlockX() - addX, barrelBSign.getLocation().getBlockY() + 1, barrelBSign.getLocation().getBlockZ() - addZ);
        isValid = Material.SPRUCE_TRAPDOOR.equals(trapRBarrelYa1.getType());
        if (!(isValid && trapRBarrelYa1.getBlockData() instanceof TrapDoor trapDRBarrelYa1 && trapDRBarrelYa1.getHalf().equals(Bisected.Half.BOTTOM) && trapDRBarrelYa1.isOpen())) {
            throw new ValidateStructureException("TrapDoorRightOfBarrel +1y is not valid!");
        }
        this.blocks.add(trapRBarrelYa1);
    }

}
