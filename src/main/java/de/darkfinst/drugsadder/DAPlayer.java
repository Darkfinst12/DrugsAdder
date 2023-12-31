package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.drug.DrugAddAddictionEvent;
import de.darkfinst.drugsadder.api.events.drug.DrugReduceAddictionEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DAPlayer {

    /**
     * The UUID of the player
     */
    @Getter
    @NotNull
    private final UUID uuid;

    /**
     * The drugs, which the player is addicted to
     */
    @Getter
    private final HashMap<DADrug, Integer> addicted = new HashMap<>();


    public DAPlayer(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Saves the player to the config
     *
     * @param players The config to save the player to
     */
    public static void save(ConfigurationSection players) {
        for (DAPlayer daPlayer : DA.loader.getDaPlayerList()) {
            if (daPlayer.addicted.isEmpty()) {
                if (players.contains(daPlayer.getUuid().toString())) {
                    players.set(daPlayer.getUuid().toString(), null);
                }
            } else {
                DA.log.debugLog("Saving player " + daPlayer.getUuid());
                ConfigurationSection player = players.createSection(daPlayer.getUuid().toString());
                daPlayer.addicted.forEach((daDrug, addictionPoints) -> player.set(daDrug.getID(), addictionPoints));
            }
        }
    }

    /**
     * Consumes the drug for the player
     *
     * @param daDrug The drug, which should be consumed
     */
    public void consumeDrug(DADrug daDrug) {
        Player player = Bukkit.getPlayer(this.uuid);
        if (player == null) {
            DA.log.errorLog("Consuming drug for offline player!");
            return;
        }
        if (daDrug.isAddictionAble()) {
            daDrug.addConsumed(this, System.currentTimeMillis());
            if (daDrug.isOverdose(this)) {
                DA.loader.msg(player, "Overdose");
                player.setHealth(0);
                daDrug.removeConsumed(this);
                return;
            }
            int addictionPointsOld = this.addicted.getOrDefault(daDrug, 0);
            int addictionPoints = addictionPointsOld + daDrug.getAddictionPoints();
            DrugAddAddictionEvent drugAddAddictionEvent = new DrugAddAddictionEvent(daDrug, addictionPointsOld, addictionPoints);
            Bukkit.getPluginManager().callEvent(drugAddAddictionEvent);
            if (!drugAddAddictionEvent.isCancelled()) {
                addictionPoints = drugAddAddictionEvent.getNewAddiction();
                this.addicted.put(daDrug, addictionPoints);
            } else {
                addictionPoints = addictionPointsOld;
            }
            if (!daDrug.getConsummation().isEmpty()) {
                int[] keys = daDrug.getConsummation().keySet().stream().mapToInt(i -> i).toArray();
                int closest = DAUtil.findClosest(keys, addictionPoints);
                daDrug.getConsummation().get(closest).forEach(daEffect -> daEffect.applyEffect(player));
            } else {
                DA.log.errorLog("Consummation of drug " + daDrug.getID() + " is empty!");
            }
        } else {
            daDrug.getDaEffects().forEach(daEffect -> daEffect.applyEffect(player));
        }
        if (daDrug.getConsumeMessage() != null) {
            DA.loader.msg(player, ChatColor.translateAlternateColorCodes('&', daDrug.getConsumeMessage().replace("%drug%", daDrug.getID())), DrugsAdderSendMessageEvent.Type.PLAYER);
        }
        if (daDrug.getConsumeTitle() != null) {
            player.sendTitle(ChatColor.translateAlternateColorCodes('&', daDrug.getConsumeTitle().replace("%drug%", daDrug.getID())), "", 10, 70, 20);
        }

        for (String serverCommand : daDrug.getServerCommands()) {
            DA.getInstance.getServer().dispatchCommand(DA.getInstance.getServer().getConsoleSender(), serverCommand.replace("%player%", player.getName()).replace("%drug%", daDrug.getID()));
        }
        for (String playerCommand : daDrug.getPlayerCommands()) {
            DA.getInstance.getServer().dispatchCommand(player, playerCommand.replace("%player%", player.getName()).replace("%drug%", daDrug.getID()));
        }
    }

    /**
     * Reduces the addictions of the player
     */
    public void reduceAddictions() {
        this.addicted.forEach((daDrug, integer) -> reduceAddiction(daDrug, false));
    }

    /**
     * Clears all addictions of the player
     */
    public void clearAddictions() {
        this.addicted.clear();
    }

    /**
     * Clears the addiction of the player for the drug
     *
     * @param daDrug The drug, which addiction should be cleared
     */
    public void clearAddiction(DADrug daDrug) {
        this.addicted.remove(daDrug);
    }

    /**
     * Reduces the addiction of the player for the drug
     * <p>
     * It executes {@link #reduceAddiction(DADrug, Integer, boolean)} with the current addiction points
     *
     * @param daDrug  The drug, which addiction should be reduced
     * @param isAsync If the reduction should be async
     */
    public void reduceAddiction(DADrug daDrug, boolean isAsync) {
        int addictionPoints = this.addicted.getOrDefault(daDrug, 0);
        reduceAddiction(daDrug, addictionPoints, isAsync);
    }

    /**
     * Reduces the addiction of the player for the drug
     * <p>
     * If the addiction points are less than zero, the addiction will be removed
     *
     * @param daDrug          The drug, which addiction should be reduced
     * @param addictionPoints The addiction points, which should be reduced
     * @param isAsync         If the reduction should be async
     */
    private void reduceAddiction(DADrug daDrug, Integer addictionPoints, boolean isAsync) {
        if (addictionPoints > 0) {
            addictionPoints -= daDrug.getReductionAmount();
            DrugReduceAddictionEvent drugReduceAddictionEvent = new DrugReduceAddictionEvent(daDrug, addictionPoints + daDrug.getReductionAmount(), addictionPoints, isAsync);
            Bukkit.getPluginManager().callEvent(drugReduceAddictionEvent);
            if (drugReduceAddictionEvent.isCancelled()) {
                return;
            }
            addictionPoints = drugReduceAddictionEvent.getNewAddiction();
            if (addictionPoints < 0) {
                addictionPoints = 0;
                this.addicted.remove(daDrug);
            } else {
                this.addicted.put(daDrug, addictionPoints);
            }

            if (!daDrug.getDeprivation().isEmpty() && this.isOnline()) {
                Player player = Bukkit.getPlayer(this.uuid);
                int[] keys = daDrug.getDeprivation().keySet().stream().mapToInt(i -> i).toArray();
                int closest = DAUtil.findClosest(keys, addictionPoints);
                if (isAsync) {
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daDrug.getDeprivation().get(closest).forEach(daEffect -> daEffect.applyEffect(player)));
                } else {
                    daDrug.getDeprivation().get(closest).forEach(daEffect -> daEffect.applyEffect(player));
                }
            }
        }
    }

    /**
     * Checks if the player is addicted to the drug
     *
     * @param daDrug The drug, which should be checked
     * @return If the player is addicted to the drug
     */
    public boolean isAddicted(DADrug daDrug) {
        return this.addicted.containsKey(daDrug);
    }

    /**
     * @return If this DAPlayer is online
     */
    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(this.uuid).isOnline();
    }

    /**
     * Adds the drug to the player
     *
     * @param drugID          The ID of the drug
     * @param addictionPoints The addiction points of the drug
     */
    public void addDrug(String drugID, int addictionPoints) {
        DADrug daDrug = DAConfig.drugReader.getDrug(drugID);
        if (daDrug == null) {
            DA.log.errorLog("Drug with ID " + drugID + " not found!");
            return;
        }
        this.addicted.put(daDrug, addictionPoints);
    }

    public int getAddiction(String drugID) {
        DADrug daDrug = DAConfig.drugReader.getDrug(drugID);
        return this.addicted.getOrDefault(daDrug, 0);
    }

    private Map<String, Integer> getAddictionString() {
        Map<String, Integer> addictionString = new HashMap<>();
        for (DADrug daDrug : this.addicted.keySet()) {
            addictionString.put(daDrug.getID(), this.addicted.get(daDrug));
        }
        return addictionString;
    }

    @Override
    public String toString() {
        return "DAPlayer{" +
                "uuid=" + uuid +
                ", addicted=" + this.getAddictionString() +
                '}';
    }
}
