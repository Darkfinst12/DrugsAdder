package de.darkfinst.DrugsAdder.Structure;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

@Setter
@Getter
public abstract class DAStructure {


    private DABody body;

    public boolean isBodyPart(Block block) {
        return this.body.blocks.contains(block);
    }
}
