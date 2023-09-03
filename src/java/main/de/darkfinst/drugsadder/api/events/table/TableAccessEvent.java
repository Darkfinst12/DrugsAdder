package de.darkfinst.drugsadder.api.events.table;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TableAccessEvent extends TableEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Block clickedBlock;
    @Getter
    private final BlockFace clickedBlockFace;
    private boolean isCancelled = false;

    public TableAccessEvent(Player player, Block clickedBlock, BlockFace clickedBlockFace) {
        this.player = player;
        this.clickedBlock = clickedBlock;
        this.clickedBlockFace = clickedBlockFace;
    }


    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    //Required by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
