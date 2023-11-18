package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
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

    public void checkForDurability(ItemStack item) {
        if (DAUtil.isItemsAdder(item)) {
            CustomStack customStack = CustomStack.byItemStack(item);
            if (customStack.hasUsagesAttribute()) {
                customStack.setUsages(customStack.getUsages() - 1);
            } else if (customStack.hasCustomDurability()) {
                customStack.setDurability(customStack.getDurability() - 1);
                if (customStack.getDurability() <= 0) {
                    item.setAmount(0);
                }
            }
        } else if (item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + 1);
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                item.setAmount(0);
            }
        }
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
                ", itemStack=" + item.getItemStack() +
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
     * This method generates a component that represents the drug.
     * <br>
     * It is used in the {@link de.darkfinst.drugsadder.commands.InfoCommand}.
     *
     * @return The component that represents the drug.
     */
    public Component asListComponent() {
        Component component = Component.text(this.ID);
        component = component.hoverEvent(this.getHover(false).asHoverEvent());
        String command = DACommandManager.buildCommandString(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.DRUGS.getArg(), this.getID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * This method generates a component that represents the drug.
     * <br>
     * It is used in the {@link de.darkfinst.drugsadder.commands.InfoCommand}.
     *
     * @return The component that represents the drug.
     */
    public Component asInfoComponent() {
        Component component = Component.text(this.ID);
        component = component.appendNewline().append(this.getHover(true));
        return component;
    }

    /**
     * Returns the drug as a component, which can be used in a message as a hover
     *
     * @param extended Whether the hover should be extended or not - For details see {@link DAAddiction#asComponent(boolean)}
     * @return The hover as a component
     */
    public Component getHover(boolean extended) {
        Component hover = Component.text().asComponent();
        hover = hover.append(Component.text("Item: " + this.item.getNamespacedID()));
        hover = hover.appendNewline().append(Component.text("isAddictionAble: " + this.isAddictionAble()));
        if (this.isAddictionAble()) {
            hover = hover.appendNewline().append(super.asComponent(extended));
        } else if (extended) {
            hover = hover.appendNewline().append(Component.text("Consummation:"));
            for (DAEffect effect : this.daEffects) {
                hover = hover.appendNewline().append(effect.asComponent());
            }
        }
        return hover;
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
