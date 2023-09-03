package de.darkfinst.drugsadder.structures;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public abstract class DABody {

    @Getter
    public final World world;
    public final List<Block> blocks = new ArrayList<>();

    public DABody(World world) {
        this.world = world;
    }

}
