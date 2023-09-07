package de.darkfinst.drugsadder;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DAAddiction {

    private boolean addictionAble;
    private int addictionPoints = -1;
    private int overdose = -1;
    private int reductionAmount = -1;
    private int reductionTime = -1;
    private boolean reductionOnlyOnline = false;

    private final Map<Integer, List<DAEffect>> deprivation = new HashMap<>();
    private final Map<Integer, List<DAEffect>> consummation = new HashMap<>();

    public DAAddiction(Boolean addictionAble) {
        this.addictionAble = addictionAble;
    }

    public void setAddiction(DAAddiction daAddiction) {
        if (!this.equals(daAddiction)) {
            this.addictionAble = daAddiction.isAddictionAble();
            this.addictionPoints = daAddiction.getAddictionPoints();
            this.overdose = daAddiction.getOverdose();
            this.reductionAmount = daAddiction.getReductionAmount();
            this.reductionTime = daAddiction.getReductionTime();
            this.reductionOnlyOnline = daAddiction.isReductionOnlyOnline();

            this.consummation.clear();
            this.consummation.putAll(daAddiction.getConsummation());

            this.deprivation.clear();
            this.deprivation.putAll(daAddiction.getDeprivation());
        }
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
