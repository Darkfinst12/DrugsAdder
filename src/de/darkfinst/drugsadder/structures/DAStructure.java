package de.darkfinst.drugsadder.structures;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.block.Block;

@Setter
@Getter
public abstract class DAStructure {


    private DABody body;

    public boolean isBodyPart(Block block) {
        return this.body.blocks.contains(block);
    }

    public World getWorld() {
        return this.body.getWorld();
    }
}
