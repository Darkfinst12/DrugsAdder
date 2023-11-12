package de.darkfinst.drugsadder.timed;

import lombok.Getter;

@Getter
public abstract class TimedExecutable implements Comparable<TimedExecutable>, Runnable {

    private final long executionTime;

    public TimedExecutable(long time) {
        this.executionTime = time;
    }

    @Override
    public int compareTo(TimedExecutable o) {
        return Long.compare(this.executionTime, o.executionTime);
    }

}
