package de.darkfinst.drugsadder.api.events.press;

import de.darkfinst.drugsadder.structures.press.DAPress;
import lombok.Getter;
import org.bukkit.event.Event;

@Getter
public abstract class PressEvent extends Event {

    private final DAPress press;

    protected PressEvent(DAPress press) {
        this.press = press;
    }
}
