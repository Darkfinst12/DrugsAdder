package de.darkfinst.drugsadder.structures.press;

import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.structures.DABody;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.WallSign;

@Getter
public class DAPressBody extends DABody {

    /**
     * The press of the body
     */
    private final DAPress press;
    /**
     * The piston of the press
     */
    private Block piston;
    /**
     * The lever of the press
     */
    private Block lever;

    public DAPressBody(DAPress press, Block sign) {
        super(sign.getWorld(), sign);
        this.press = press;
    }

    /**
     * Checks if the press is valid
     *
     * @return true, if the press is valid otherwise false
     * @throws ValidateStructureException if the press is not valid
     */
    //-x= west +x=east -z=north +z=south
    public boolean isValidPress() throws ValidateStructureException {
        this.blocks.add(this.sign);
        World world = this.sign.getWorld();

        WallSign wallSign = (WallSign) this.sign.getBlockData();
        BlockFace face = wallSign.getFacing();

        this.checkBlocksOverSign(world, face);
        this.checkBlocksBehindSign(world, face);

        return true;
    }

    /**
     * Checks the blocks over the sign of the press if they are valid
     *
     * @param world The world of the press
     * @param face  The face of the sign
     */
    private void checkBlocksOverSign(World world, BlockFace face) {
        Location loc = this.sign.getLocation();
        Block blockOSign = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
        boolean isValid = Material.AIR.equals(blockOSign.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockOverSign is not valid!");
        }
        this.blocks.add(blockOSign);

        Block blockOSignY1 = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ());
        isValid = Material.LEVER.equals(blockOSignY1.getType());
        if (!(isValid && !blockOSignY1.isBlockPowered() && blockOSignY1.getRelative(BlockFace.DOWN, 2).equals(this.sign))) {
            throw new ValidateStructureException("BlockOverSign +1y is not valid!");
        }
        this.lever = blockOSignY1;
        this.blocks.add(blockOSignY1);

        Block blockOSignY2 = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 3, loc.getBlockZ());
        isValid = Material.SMOOTH_QUARTZ_STAIRS.equals(blockOSignY2.getType());
        if (!(isValid && blockOSignY2.getBlockData() instanceof Stairs stairOSign && Bisected.Half.BOTTOM.equals(stairOSign.getHalf()) && stairOSign.getFacing().equals(face.getOppositeFace()))) {
            throw new ValidateStructureException("BlockOverSign +2y is not valid!");
        }
        this.blocks.add(blockOSignY2);
    }

    /**
     * Checks the blocks behind the sign of the press if they are valid
     *
     * @param world The world of the press
     * @param face  The face of the sign
     */
    private void checkBlocksBehindSign(World world, BlockFace face) {
        Block anvilBSign = this.sign.getRelative(face.getOppositeFace());
        boolean isValid = Material.ANVIL.equals(anvilBSign.getType());
        if (!(isValid && anvilBSign.getBlockData() instanceof Directional anvilDirection && !anvilDirection.getFacing().equals(face) && !anvilDirection.getFacing().equals(face.getOppositeFace()))) {
            throw new ValidateStructureException("AnvilBehindSign is not valid!");
        }
        this.blocks.add(anvilBSign);

        Block blockBSignYa1 = world.getBlockAt(anvilBSign.getLocation().getBlockX(), anvilBSign.getLocation().getBlockY() + 1, anvilBSign.getLocation().getBlockZ());
        isValid = Material.AIR.equals(blockBSignYa1.getType()) || Material.PISTON_HEAD.equals(blockBSignYa1.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockBehindSign +1y is not valid!");
        }
        this.blocks.add(blockBSignYa1);

        Block blockBSignYa2 = world.getBlockAt(anvilBSign.getLocation().getBlockX(), anvilBSign.getLocation().getBlockY() + 2, anvilBSign.getLocation().getBlockZ());
        isValid = Material.PISTON.equals(blockBSignYa2.getType());
        if (!(isValid && blockBSignYa2.getBlockData() instanceof Piston pistonBlock && pistonBlock.getFacing().equals(BlockFace.DOWN))) {
            throw new ValidateStructureException("BlockBehindSign +2y is not valid!");
        }
        this.piston = blockBSignYa2;
        this.blocks.add(blockBSignYa2);

        Block blockBSignYa3 = world.getBlockAt(anvilBSign.getLocation().getBlockX(), anvilBSign.getLocation().getBlockY() + 3, anvilBSign.getLocation().getBlockZ());
        isValid = Material.IRON_BLOCK.equals(blockBSignYa3.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockBehindSign +3y is not valid!");
        }
        this.blocks.add(blockBSignYa3);

        Block blockBAnvil = anvilBSign.getRelative(face.getOppositeFace());
        isValid = Material.IRON_BLOCK.equals(blockBAnvil.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockBehindAnvil is not valid!");
        }
        this.blocks.add(blockBAnvil);

        Block blockBAnvilYa1 = world.getBlockAt(blockBAnvil.getLocation().getBlockX(), blockBAnvil.getLocation().getBlockY() + 1, blockBAnvil.getLocation().getBlockZ());
        isValid = Material.MAGMA_BLOCK.equals(blockBAnvilYa1.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockBehindAnvil +1y is not valid!");
        }
        this.blocks.add(blockBAnvilYa1);

        Block blockBAnvilYa2 = world.getBlockAt(blockBAnvil.getLocation().getBlockX(), blockBAnvil.getLocation().getBlockY() + 2, blockBAnvil.getLocation().getBlockZ());
        isValid = Material.IRON_BLOCK.equals(blockBAnvilYa2.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockBehindAnvil +2y is not valid!");
        }
        this.blocks.add(blockBAnvilYa2);

        Block blockBAnvilYa3 = world.getBlockAt(blockBAnvil.getLocation().getBlockX(), blockBAnvil.getLocation().getBlockY() + 3, blockBAnvil.getLocation().getBlockZ());
        isValid = Material.SMOOTH_QUARTZ_STAIRS.equals(blockBAnvilYa3.getType());
        if (!(isValid && blockBAnvilYa3.getBlockData() instanceof Stairs stairBAnvil && Bisected.Half.BOTTOM.equals(stairBAnvil.getHalf()) && stairBAnvil.getFacing().equals(face))) {
            throw new ValidateStructureException("BlockBehindAnvil +3y is not valid!");
        }
        this.blocks.add(blockBAnvilYa3);
    }


}
