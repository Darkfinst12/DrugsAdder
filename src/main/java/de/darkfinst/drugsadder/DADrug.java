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

    /**
     * The ID of the drug for identification
     */
    @NotNull
    private final String ID;
    /**
     * The item, which represents the drug
     */
    @NotNull
    private final DAItem item;
    /**
     * The commands, which should be executed on the server, when the drug is consumed
     */
    private final List<String> serverCommands = new ArrayList<>();
    /**
     * The commands, which should be executed on the player, when the drug is consumed
     */
    private final List<String> playerCommands = new ArrayList<>();
    /**
     * The effects, which should be applied to the player, when the drug is consumed (see {@link DAEffect})
     * <p>
     * If the drug is addiction able, the effects will only be applied if in the addiction no effects are found
     * <br>
     * For details see {@link DAAddiction}
     */
    private final List<DAEffect> daEffects = new ArrayList<>();
    /**
     * The message, which should be sent to the player, when the drug is consumed
     */
    @Nullable
    private final String consumeMessage;
    /**
     * The title, which should be sent to the player, when the drug is consumed
     */
    @Nullable
    private final String consumeTitle;
    /**
     * The match types, which should be used to match the item, which represents the drug
     */
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

    /**
     * Consumes this drug for the given player
     * <p>
     * If the Player has no corresponding DAPlayer, a new one will be created
     *
     * @param player The player, which consumes the drug
     */
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

    /**
     * Starts a repeating task, which reduces the addiction of all addicted players
     */
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

    /**
     * A runnable, which reduces the addiction of all addicted players
     */
    public static class DrugsReductionRunnable implements Runnable {

        /**
         * The drug, which should be reduced
         */
        private final DADrug daDrug;
        private final boolean isAsync;

        public DrugsReductionRunnable(DADrug daDrug, boolean isAsync) {
            this.daDrug = daDrug;
            this.isAsync = isAsync;
        }

        /**
         * If the drug is reduction only online, only online players will be reduced otherwise all players will be reduced
         */
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
