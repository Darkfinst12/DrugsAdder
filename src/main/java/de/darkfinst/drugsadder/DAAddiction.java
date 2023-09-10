package de.darkfinst.drugsadder;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class DAAddiction {

    private boolean addictionAble;
    private int addictionPoints = -1;
    private int overdose = -1;
    private int reductionAmount = -1;
    private int reductionTime = -1;
    private long overdoseTime = -1;
    private boolean reductionOnlyOnline = false;

    private final Map<Integer, List<DAEffect>> deprivation = new HashMap<>();
    private final Map<Integer, List<DAEffect>> consummation = new HashMap<>();

    private final Map<DAPlayer, List<Long>> lastConsummations = new HashMap<>();

    public DAAddiction(Boolean addictionAble) {
        this.addictionAble = addictionAble;
    }

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

    public void addConsumed(DAPlayer daPlayer, long time) {
        if (this.overdose > 1) {
            DA.log.debugLog("Adding consumed drug to player " + daPlayer.getUuid() + " at time " + time);
            List<Long> consumed = this.lastConsummations.getOrDefault(daPlayer, null);
            if (consumed == null) {
                consumed = new ArrayList<>();
            }
            consumed.add(time);
            this.lastConsummations.put(daPlayer, consumed);
        }
    }

    public void removeConsumed(DAPlayer daPlayer) {
        this.lastConsummations.remove(daPlayer);
    }

    public boolean isOverdose(DAPlayer daPlayer) {
        boolean isOverdose = false;
        if (this.overdose > 1 && this.overdoseTime > 1) {
            DA.loader.debugLog("Checking overdose for player " + daPlayer.getUuid());
            List<Long> consumed = this.lastConsummations.getOrDefault(daPlayer, null);
            long time = System.currentTimeMillis();

            if (consumed != null) {
                DA.log.debugLog("Consumed is not null");
                consumed.iterator().forEachRemaining(consumeTime -> {
                    if (time - consumeTime >= TimeUnit.SECONDS.toMillis(this.overdoseTime)) {
                        consumed.remove(consumeTime);
                    }
                });
                DA.log.debugLog("Consumed: " + consumed);
                if (consumed.size() > this.overdose) {
                    isOverdose = true;
                }
            }
        }
        DA.log.debugLog("Is overdose: " + isOverdose);
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

}
