package de.darkfinst.drugsadder;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class DAAddiction {

    private final Boolean isAddictionAble;
    private int addictionPoints;
    private int addictionStart;
    private int overdose;
    private int reductionAmount;
    private int reductionTime;
    private boolean reductionOnlyOnline;
    private final Map<Integer, DAEffect> deprivation = new HashMap<>();

    public DAAddiction(Boolean isAddictionAble) {
        this.isAddictionAble = isAddictionAble;
    }
}
