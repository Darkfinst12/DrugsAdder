package de.darkfinst.drugsadder;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DADrug extends DAAddiction {

    @NotNull
    private final String ID;
    @NotNull
    private final ItemStack itemStack;
    private final List<String> serverCommands = new ArrayList<>();
    private final List<String> playerCommands = new ArrayList<>();
    private final List<DAEffect> daEffects = new ArrayList<>();
    @Nullable
    private final String consumeMessage;
    @Nullable
    private final String consumeTitle;
    @NotNull
    private final ItemMatchType[] matchTypes;

    public DADrug(@NotNull String ID, @NotNull ItemStack itemStack, @Nullable String consumeMessage, @Nullable String consumeTitle, @NotNull ItemMatchType... matchTypes) {
        super(false);
        this.ID = ID;
        this.itemStack = itemStack;
        this.consumeMessage = consumeMessage;
        this.consumeTitle = consumeTitle;
        this.matchTypes = matchTypes;
    }

    public void consume(Player player) {

    }

}
