package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class DADrug extends DAAddiction {

    @NotNull
    private final String ID;
    @NotNull
    private final DAItem item;
    private final List<String> serverCommands = new ArrayList<>();
    private final List<String> playerCommands = new ArrayList<>();
    private final List<DAEffect> daEffects = new ArrayList<>();
    @Nullable
    private final String consumeMessage;
    @Nullable
    private final String consumeTitle;
    @NotNull
    private final ItemMatchType[] matchTypes;

    public DADrug(@NotNull String ID, @NotNull DAItem item, @Nullable String consumeMessage, @Nullable String consumeTitle, @NotNull ItemMatchType... matchTypes) {
        super(false);
        this.ID = ID;
        this.item = item;
        this.consumeMessage = consumeMessage;
        this.consumeTitle = consumeTitle;
        this.matchTypes = matchTypes;
    }

    public void consume(Player player) {
        DAPlayer daPlayer = DA.loader.getDaPlayer(player);
        if (daPlayer == null) {
            daPlayer = new DAPlayer(player.getUniqueId());
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

    @Override
    public String toString() {
        return "DADrug{" +
                "ID='" + ID + '\'' +
                ", itemStack=" + item.getNamespacedID() +
                ", serverCommands=" + serverCommands +
                ", playerCommands=" + playerCommands +
                ", daEffects=" + daEffects +
                ", consumeMessage='" + consumeMessage + '\'' +
                ", consumeTitle='" + consumeTitle + '\'' +
                ", matchTypes=" + Arrays.toString(matchTypes) +
                ", addiction=" + super.toString() +
                '}';
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
            for (DAPlayer daPlayer : DA.loader.getDaPlayerList().stream().filter(daPlayer -> daPlayer.isAddicted(this.daDrug)).toList()) {
                if (this.daDrug.isReductionOnlyOnline() && daPlayer.isOnline()) {
                    daPlayer.reduceAddiction(this.daDrug, isAsync);
                } else if (!this.daDrug.isReductionOnlyOnline()) {
                    daPlayer.reduceAddiction(this.daDrug, isAsync);
                }
            }
        }
    }


}
