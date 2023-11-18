package de.darkfinst.drugsadder;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class DAAddiction {

    /**
     * The effects, which are applied to the player, when he is addicted and the drug is not consumed
     * <br>
     * It will be accessed by the reduction timer
     */
    private final Map<Integer, List<DAEffect>> deprivation = new HashMap<>();
    /**
     * The effects, which are applied to the player, when the drug is consumed
     */
    private final Map<Integer, List<DAEffect>> consummation = new HashMap<>();
    /**
     * The last consummations of the players
     * <br>
     * In {@link DAAddiction#isOverdose(DAPlayer)} explained as logs
     */
    private final Map<DAPlayer, ConcurrentLinkedDeque<Long>> lastConsummations = new HashMap<>();
    /**
     * Whether the drug is addiction able or not
     */
    private boolean addictionAble;
    /**
     * The points, which are added to the addiction, when the drug is consumed
     */
    private int addictionPoints = -1;
    /**
     * The amount of consummations, which are needed to get an overdose
     */
    private int overdose = -1;
    /**
     * The points, which are removed when the reduction time is over
     */
    private int reductionAmount = -1;
    /**
     * The time, which has to pass, until the reduction amount is removed
     */
    private int reductionTime = -1;
    /**
     * The timespan in which the player overdoses, if the drug is consumed to often
     */
    private long overdoseTime = -1;
    /**
     * Whether the reduction should only be applied when the player is online or not
     */
    private boolean reductionOnlyOnline = false;

    public DAAddiction(Boolean addictionAble) {
        this.addictionAble = addictionAble;
    }

    /**
     * Sets the addiction to the given addiction
     *
     * @param daAddiction The addiction to set
     */
    public void setAddiction(DAAddiction daAddiction) {
        if (!this.equals(daAddiction)) {
            this.addictionAble = daAddiction.isAddictionAble();
            this.addictionPoints = daAddiction.getAddictionPoints();
            this.overdose = daAddiction.getOverdose();
            this.overdoseTime = daAddiction.getOverdoseTime();
            this.reductionAmount = daAddiction.getReductionAmount();
            this.reductionTime = daAddiction.getReductionTime();
            this.reductionOnlyOnline = daAddiction.isReductionOnlyOnline();

            this.consummation.clear();
            this.consummation.putAll(daAddiction.getConsummation());

            this.deprivation.clear();
            this.deprivation.putAll(daAddiction.getDeprivation());
        }
    }

    /**
     * Adds a log of a player, which consumed the drug with this addiction
     *
     * @param daPlayer The player, which consumed the drug
     * @param time     The time, when the player consumed the drug
     */
    public void addConsumed(DAPlayer daPlayer, long time) {
        if (this.overdose > 1) {
            ConcurrentLinkedDeque<Long> consumed = this.lastConsummations.getOrDefault(daPlayer, null);
            if (consumed == null) {
                consumed = new ConcurrentLinkedDeque<>();
            }
            consumed.add(time);
            this.lastConsummations.put(daPlayer, consumed);
        }
    }

    /**
     * Removes the log of a player, which consumed the drug with this addiction
     *
     * @param daPlayer The player, which consumed the drug
     */
    public void removeConsumed(DAPlayer daPlayer) {
        this.lastConsummations.remove(daPlayer);
    }

    /**
     * Checks if the player has an overdose
     * <p>
     * Also updates the logs of the player if they are outdated
     * <br>
     * If the player has an overdose, the logs will be cleared
     *
     * @param daPlayer The player to check
     * @return true, if the player has an overdose otherwise false
     */
    public boolean isOverdose(DAPlayer daPlayer) {
        boolean isOverdose = false;
        if (this.overdose > 1 && this.overdoseTime > 1) {
            ConcurrentLinkedDeque<Long> consumed = this.lastConsummations.getOrDefault(daPlayer, null);
            long time = System.currentTimeMillis();

            if (!consumed.isEmpty()) {
                consumed.removeIf(consumeTime -> time - consumeTime >= TimeUnit.SECONDS.toMillis(this.overdoseTime));
                if (consumed.size() > this.overdose) {
                    isOverdose = true;
                }
            }
        }
        return isOverdose;
    }

    @Override
    public String toString() {
        return "DAAddiction{" +
                "addictionAble=" + addictionAble +
                ", addictionPoints=" + addictionPoints +
                ", overdose=" + overdose +
                ", reductionAmount=" + reductionAmount +
                ", reductionTime=" + reductionTime +
                ", reductionOnlyOnline=" + reductionOnlyOnline +
                ", deprivation=" + deprivation +
                ", consummation=" + consummation +
                '}';
    }

    /**
     * Returns the addiction as a component
     *
     * @param extended Whether the component should be extended or not (includes the effects)
     * @return The addiction as a component
     */
    protected Component asComponent(boolean extended) {
        Component component = Component.text().asComponent();
        component = component.append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_AddictionPoints", this.addictionPoints + ""));
        component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Overdose", this.overdose + ""));
        component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_OverdoseTime", this.overdoseTime + ""));
        component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ReductionAmount", this.reductionAmount + ""));
        component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ReductionTime", this.reductionTime + ""));
        component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ReductionOnlyOnline", this.reductionOnlyOnline + ""));
        if (extended) {
            component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Deprivation"));
            for (Map.Entry<Integer, List<DAEffect>> entry : this.deprivation.entrySet()) {
                component = component.appendNewline().append(Component.text("- " + entry.getKey() + ":"));
                for (DAEffect effect : entry.getValue()) {
                    component = component.appendNewline().append(effect.asComponent());
                }
            }
            component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Consummation"));
            for (Map.Entry<Integer, List<DAEffect>> entry : this.consummation.entrySet()) {
                component = component.appendNewline().append(Component.text("- " + entry.getKey() + ":"));
                for (DAEffect effect : entry.getValue()) {
                    component = component.appendNewline().append(effect.asComponent());
                }
            }
        }
        return component;
    }
}
