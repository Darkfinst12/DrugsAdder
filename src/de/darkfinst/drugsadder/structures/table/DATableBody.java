package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.structures.DABody;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
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

    private final DATable table;
    private final Block sign;

    public DATableBody(DATable table, Block sign) {
        super(sign.getWorld());
        this.table = table;
        this.sign = sign;
    }

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
