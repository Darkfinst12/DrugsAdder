package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.structures.DABody;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.WallSign;

@Getter
public class DATableBody extends DABody {

    /**
     * The sign of the table
     */
    private final Block sign;

    /**
     * The process of the table
     */
    private final DATableProcess process = new DATableProcess();

    public DATableBody(Block sign) {
        super(sign.getWorld());
        this.sign = sign;
    }

    /**
     * Checks if the table is valid
     *
     * @return true, if the table is valid otherwise false
     * @throws ValidateStructureException if the table is not valid
     */
    //-x= west +x=east -z=north +z=south
    public boolean isValidTable() throws ValidateStructureException {
        this.blocks.add(this.sign);
        World world = this.sign.getWorld();

        WallSign wallSign = (WallSign) this.sign.getBlockData();
        BlockFace face = wallSign.getFacing();
        BlockFace direction = this.getDirection(face);
        int[] xz = this.getXZValues(direction);

        Block stairBSign = this.sign.getRelative(face.getOppositeFace());
        boolean isValid = Material.SMOOTH_QUARTZ_STAIRS.equals(stairBSign.getType());
        if (!(isValid && stairBSign.getBlockData() instanceof Stairs stairBSignD && Bisected.Half.TOP.equals(stairBSignD.getHalf()) && stairBSignD.getFacing().equals(direction))) {
            throw new ValidateStructureException("StairBehindSign is not valid!");
        }
        this.blocks.add(stairBSign);

        Block stairUStair = world.getBlockAt(stairBSign.getLocation().getBlockX(), stairBSign.getLocation().getBlockY() - 1, stairBSign.getLocation().getBlockZ());
        isValid = Material.SMOOTH_QUARTZ_STAIRS.equals(stairUStair.getType());
        if (!(isValid && stairUStair.getBlockData() instanceof Stairs stairUStairD && Bisected.Half.BOTTOM.equals(stairUStairD.getHalf()) && stairUStairD.getFacing().equals(direction))) {
            throw new ValidateStructureException("StairUnderStair is not valid!");
        }
        this.blocks.add(stairUStair);

        Block brewingOStair = world.getBlockAt(stairBSign.getLocation().getBlockX(), stairBSign.getLocation().getBlockY() + 1, stairBSign.getLocation().getBlockZ());
        isValid = Material.BREWING_STAND.equals(brewingOStair.getType());
        if (!isValid) {
            throw new ValidateStructureException("BrewingStandOnStair is not valid!");
        }
        this.blocks.add(brewingOStair);

        this.checkBlockLineOne(world, stairBSign, xz, direction);
        this.checkBlockLineTwo(world, stairBSign, xz, direction);

        return true;
    }

    /**
     * Checks the blocks behind the sign of the table if they are valid - On the same Y line
     *
     * @param world      The world of the table
     * @param stairBSign The stair behind the sign
     * @param xz         The x and z values
     * @param direction  The direction of the table
     */
    private void checkBlockLineOne(World world, Block stairBSign, int[] xz, BlockFace direction) {
        boolean isValid;
        Block blockNStair = world.getBlockAt(stairBSign.getLocation().getBlockX() + xz[0], stairBSign.getLocation().getBlockY(), stairBSign.getLocation().getBlockZ() + xz[1]);
        isValid = Material.WATER_CAULDRON.equals(blockNStair.getType());
        if (!(isValid && blockNStair.getBlockData() instanceof Levelled cauldron && cauldron.getLevel() == 3)) {
            throw new ValidateStructureException("BlockNextToStair is not valid! - " + (blockNStair.getBlockData() instanceof Levelled) + " - ");
        }
        this.blocks.add(blockNStair);

        Block blockNa1Stair = world.getBlockAt(stairBSign.getLocation().getBlockX() + (xz[0] * 2), stairBSign.getLocation().getBlockY(), stairBSign.getLocation().getBlockZ() + (xz[1] * 2));
        isValid = Material.SMOOTH_QUARTZ_SLAB.equals(blockNa1Stair.getType());
        if (!(isValid && blockNa1Stair.getBlockData() instanceof Slab slab && Slab.Type.TOP.equals(slab.getType()))) {
            throw new ValidateStructureException("BlockNextToStair +1 is not valid!");
        }
        this.blocks.add(blockNa1Stair);

        Block blockNa2Stair = world.getBlockAt(stairBSign.getLocation().getBlockX() + (xz[0] * 3), stairBSign.getLocation().getBlockY(), stairBSign.getLocation().getBlockZ() + (xz[1] * 3));
        isValid = Material.SMOOTH_QUARTZ_STAIRS.equals(blockNa2Stair.getType());
        if (!(isValid && blockNa2Stair.getBlockData() instanceof Stairs blockNa2StairD && Bisected.Half.TOP.equals(blockNa2StairD.getHalf()) && blockNa2StairD.getFacing().equals(direction.getOppositeFace()))) {
            throw new ValidateStructureException("BlockNextToStair +2 is not valid!");
        }
        this.blocks.add(blockNa2Stair);
    }

    /**
     * Checks the blocks behind the sign of the table if they are valid - Under the Y line
     *
     * @param world      The world of the table
     * @param stairBSign The stair behind the sign
     * @param xz         The x and z factors for more explanation see {@link #getXZValues(BlockFace)}
     * @param direction  The direction of the table
     */
    private void checkBlockLineTwo(World world, Block stairBSign, int[] xz, BlockFace direction) {
        boolean isValid;
        Block blockNUStair = world.getBlockAt(stairBSign.getLocation().getBlockX() + xz[0], stairBSign.getLocation().getBlockY() - 1, stairBSign.getLocation().getBlockZ() + xz[1]);
        isValid = Material.CAMPFIRE.equals(blockNUStair.getType());
        if (!(isValid && blockNUStair.getBlockData() instanceof Lightable campLight && !campLight.isLit())) {
            throw new ValidateStructureException("BlockUnderAndNextToStair is not valid!");
        }
        this.blocks.add(blockNUStair);

        Block blockNUa1Stair = world.getBlockAt(stairBSign.getLocation().getBlockX() + (xz[0] * 2), stairBSign.getLocation().getBlockY() - 1, stairBSign.getLocation().getBlockZ() + (xz[1] * 2));
        isValid = Material.CHEST.equals(blockNUa1Stair.getType());
        if (!isValid) {
            throw new ValidateStructureException("BlockUnderAndNextToStair +1 is not valid!");
        }
        this.blocks.add(blockNUa1Stair);

        Block blockNUa2Stair = world.getBlockAt(stairBSign.getLocation().getBlockX() + (xz[0] * 3), stairBSign.getLocation().getBlockY() - 1, stairBSign.getLocation().getBlockZ() + (xz[1] * 3));
        isValid = Material.SMOOTH_QUARTZ_STAIRS.equals(blockNUa2Stair.getType());
        if (!(isValid && blockNUa2Stair.getBlockData() instanceof Stairs blockNUa2StairD && Bisected.Half.BOTTOM.equals(blockNUa2StairD.getHalf()) && blockNUa2StairD.getFacing().equals(direction.getOppositeFace()))) {
            throw new ValidateStructureException("BlockUnderAndNextToStair +2 is not valid!");
        }
        this.blocks.add(blockNUa2Stair);
    }

    /**
     * Gets the direction of the table
     *
     * @param face The face of the sign
     * @return The direction of the table
     */
    private BlockFace getDirection(BlockFace face) {
        BlockFace direction;
        switch (face) {
            case NORTH -> direction = BlockFace.EAST;
            case SOUTH -> direction = BlockFace.WEST;
            case EAST -> direction = BlockFace.SOUTH;
            case WEST -> direction = BlockFace.NORTH;
            default -> direction = null;
        }
        return direction;
    }

    /**
     * Gets the x and z values of the table
     * <p>
     * It is used to check the blocks behind the sign of the table
     * <p>
     * The values are -1, 0 or 1 depending on the direction of the table
     * it will be added to the corresponding x or z value
     *
     * @param face The face of the sign
     * @return The x and z values of the table
     */
    private int[] getXZValues(BlockFace face) {
        int[] integers;
        switch (face) {
            case NORTH -> integers = new int[]{0, 1};
            case SOUTH -> integers = new int[]{0, -1};
            case EAST -> integers = new int[]{-1, 0};
            case WEST -> integers = new int[]{1, 0};
            default -> integers = new int[]{0, 0};
        }
        return integers;
    }

}
