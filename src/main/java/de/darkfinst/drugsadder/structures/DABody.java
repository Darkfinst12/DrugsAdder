package de.darkfinst.drugsadder.structures;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public abstract class DABody {

    /**
     * The world of the body
     */
    @Getter
    public final World world;

    /**
     * The sign of the press
     */
    @Getter
    public final Block sign;
    /**
     * The blocks of the body
     */
    public final List<Block> blocks = new ArrayList<>();

    public DABody(World world, Block sign) {
        this.world = world;
        this.sign = sign;
    }

}
