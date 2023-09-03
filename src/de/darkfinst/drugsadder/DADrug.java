package de.darkfinst.drugsadder;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DADrug extends DAAddiction {

    @NotNull
    private final String ID;
    @NotNull
    private final ItemStack itemStack;
    private final List<String> serverCommands = new ArrayList<>();
    private final List<String> playerCommands = new ArrayList<>();
    private final List<DAEffect> daEffects = new ArrayList<>();
    @Nullable
    private final String consumeMessage;
    @Nullable
    private final String consumeTitle;
    @NotNull
    private final ItemMatchType[] matchTypes;

    public DADrug(@NotNull String ID, @NotNull ItemStack itemStack, @Nullable String consumeMessage, @Nullable String consumeTitle, @NotNull ItemMatchType... matchTypes) {
        super(false);
        this.ID = ID;
        this.itemStack = itemStack;
        this.consumeMessage = consumeMessage;
        this.consumeTitle = consumeTitle;
        this.matchTypes = matchTypes;
    }

    public void consume(Player player) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(player);
        if (daPlayer == null) {
            daPlayer = new DAPlayer(player);
            if (this.isAddictionAble()) {
                DA.loader.addDaPlayer(daPlayer);
            }
        }
        daPlayer.consumeDrug(this);
    }

    public void registerReductionTask() {
        if (this.getReductionTime() > 0) {
            long time = this.getReductionTime() * 60L * 20L;
            DA.getInstance.getServer().getScheduler().runTaskTimerAsynchronously(DA.getInstance, new DrugsReductionRunnable(this, true), time, time);
        }
    }

    public static class DrugsReductionRunnable implements Runnable {

        private final DADrug daDrug;
        private final boolean isAsync;

        public DrugsReductionRunnable(DADrug daDrug, boolean isAsync) {
            this.daDrug = daDrug;
            this.isAsync = isAsync;
        }

        @Override
        public void run() {
            for (DAPlayer daPlayer : DA.loader.getDaPlayerList()) {
                if (this.daDrug.isReductionOnlyOnline() && daPlayer.getPlayer().isOnline()) {
                    daPlayer.reduceAddiction(this.daDrug, isAsync);
                } else if (!this.daDrug.isReductionOnlyOnline()) {
                    daPlayer.reduceAddiction(this.daDrug, isAsync);
                }
            }
        }
    }

}
