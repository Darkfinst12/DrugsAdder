package de.darkfinst.drugsadder;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DADrug extends DAAddiction {

    private final String ID;
    private final ItemStack itemStack;
    private final List<String> serverCommands = new ArrayList<>();
    private final List<String> playerCommands = new ArrayList<>();
    private final String consumeMessage;
    private final String consumeTitle;
    private final MatchType[] matchTypes;
    private final List<DAEffect> effects = new ArrayList<>();

    public DADrug(String ID, ItemStack itemStack, Boolean addictionAble, String consumeMessage, String consumeTitle, MatchType... matchTypes) {
        super(addictionAble);
        this.ID = ID;
        this.itemStack = itemStack;
        this.consumeMessage = consumeMessage;
        this.consumeTitle = consumeTitle;
        this.matchTypes = matchTypes;
    }

    public void consume(Player player) {

    }

    public enum MatchType {
        CMD,
        NAME,
        LORE,
        ALL
    }

}
