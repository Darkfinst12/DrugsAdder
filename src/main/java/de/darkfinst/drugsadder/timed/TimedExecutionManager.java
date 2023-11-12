package de.darkfinst.drugsadder.timed;

import java.util.PriorityQueue;

public class TimedExecutionManager {

    private final PriorityQueue<TimedExecutable> executables = new PriorityQueue<>();

    public void addExecutable(TimedExecutable executable) {
        this.executables.add(executable);
    }

    public void removeExecutable(TimedExecutable executable) {
        this.executables.remove(executable);
    }

    public void tick() {
        long currentTime = System.currentTimeMillis();
        while (true) {
            TimedExecutable next = this.executables.peek();
            if (next == null || next.getExecutionTime() > currentTime) {
                break;
            }
            next.run();
            this.executables.poll();
        }
    }

}
