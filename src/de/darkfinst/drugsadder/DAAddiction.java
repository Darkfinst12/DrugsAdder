package de.darkfinst.drugsadder;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class DAAddiction {

    private final Boolean isAddictionAble;
    private int addictionPoints = -1;
    private int addictionStart = -1;
    private int overdose = -1;
    private int reductionAmount = -1;
    private int reductionTime = -1;
    private boolean reductionOnlyOnline = false;

    private final Map<Integer, DAEffect> deprivation = new HashMap<>();

    public DAAddiction(Boolean isAddictionAble) {
        this.isAddictionAble = isAddictionAble;
    }
}
