package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.barrel.BarrelProcessMaterialsEvent;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.exceptions.Structures.Barrel.BarrelException;
import de.darkfinst.drugsadder.exceptions.Structures.Barrel.NotEnoughMaterialsException;
import de.darkfinst.drugsadder.exceptions.Structures.Barrel.NotEnoughTimePassedException;
import de.darkfinst.drugsadder.exceptions.Structures.Barrel.TooMuchTimePassedException;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public class DABarrelRecipe extends DARecipe {

    /**
     * The time the recipe needs to process
     * <br>
     * The time is in Minutes
     */
    private final long processTime;

    /**
     * The time the recipe can process longer than the process time
     * <br>
     * The time is in Minutes
     */
    private final long processOverdueAcceptance;

    public DABarrelRecipe(String recipeID, RecipeType recipeType, long processTime, long processOverdueAcceptance, DAItem result, DAItem... materials) {
        super(recipeID, recipeType, result, materials);
        this.processTime = processTime;
        this.processOverdueAcceptance = processOverdueAcceptance;
    }

    /**
     * Processes the materials in the barrel
     *
     * @param barrel The barrel to process
     */
    public void processMaterials(DABarrel barrel) {
        List<ItemStack> lItems = this.getInventoryContents(barrel);
        if (this.hasMaterials(lItems.toArray(new ItemStack[0]))) {
            long current = System.currentTimeMillis();
            BarrelProcessMaterialsEvent event = new BarrelProcessMaterialsEvent(this);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            try {
                int fallback = 0;
                while (this.hasMaterials(this.getInventoryContents(barrel).toArray(new ItemStack[0]))) {
                    if (fallback >= 100) {
                        break;
                    }
                    this.addResult(barrel, current);
                    fallback++;
                }
            } catch (BarrelException ignored) {
                //No Materials need to be removed
            }
        }
    }

    /**
     * Returns the contents of the barrel without the time stamps
     *
     * @param barrel The barrel to get the contents from
     * @return The contents of the barrel without the time stamps
     */
    private @NotNull List<ItemStack> getInventoryContents(DABarrel barrel) {
        List<ItemStack> lItems = new ArrayList<>();
        for (ItemStack storageContent : barrel.getInventory().getContents().clone()) {
            if (storageContent != null && !Material.AIR.equals(storageContent.getType())) {
                ItemStack stack = storageContent.clone();
                barrel.removeTimeStamp(stack);
                lItems.add(stack);
            } else {
                lItems.add(storageContent);
            }
        }
        return lItems;
    }

    /**
     * Adds the result to the barrel
     *
     * @param barrel  The barrel to add the result to
     * @param current The current time
     * @throws NotEnoughMaterialsException  If the recipe doesn't have enough materials
     * @throws NotEnoughTimePassedException If the recipe hasn't processed long enough
     * @throws TooMuchTimePassedException   If the recipe has processed too long
     */
    private void addResult(DABarrel barrel, long current) throws NotEnoughMaterialsException, NotEnoughTimePassedException, TooMuchTimePassedException {
        Map<Integer, DAItem> modifiedSlots = new HashMap<>();
        for (DAItem material : this.getMaterials()) {
            List<ItemStack> lItems = this.getInventoryContents(barrel);
            for (int i = 0; i < lItems.size(); i++) {
                if (DAUtil.matchItems(material.getItemStack(), lItems.get(i), material.getItemMatchTypes())) {
                    long passedTime = current - barrel.getTimeStamp(barrel.getInventory().getItem(i));
                    long passedSeconds = TimeUnit.MILLISECONDS.toSeconds(passedTime);

                    if (material.getAmount() > lItems.get(i).getAmount()) {
                        throw new NotEnoughMaterialsException();
                    }
                    if (passedSeconds < TimeUnit.MINUTES.toSeconds(this.getProcessTime())) {
                        throw new NotEnoughTimePassedException();
                    }

                    if (passedSeconds > TimeUnit.MINUTES.toSeconds(this.getProcessTime()) + TimeUnit.MINUTES.toSeconds(this.getProcessOverdueAcceptance())) {
                        throw new TooMuchTimePassedException();
                    }
                    modifiedSlots.put(i, material);
                }
            }
        }
        if (!modifiedSlots.isEmpty()) {
            for (Integer slotBarrel : modifiedSlots.keySet()) {
                DAItem material = modifiedSlots.get(slotBarrel);
                ItemStack itemStack = barrel.getInventory().getItem(slotBarrel);
                if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                    int newAmount = itemStack.getAmount() - material.getAmount();
                    if (newAmount <= 0) {
                        barrel.getInventory().clear(slotBarrel);
                    } else {
                        itemStack.setAmount(newAmount);
                    }
                }
            }
            HashMap<Integer, ItemStack> notAdded = barrel.getInventory().addItem(this.getResult().getItemStack());
            if (!notAdded.isEmpty()) {
                for (ItemStack itemStack : notAdded.values()) {
                    barrel.getBody().getWorld().dropItemNaturally(barrel.getBody().getSign().getLocation(), itemStack);
                }
            }
        }
    }

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DABarrelRecipe")
                .replace("}", "") +
                ", processTime=" + processTime +
                ", processOverdueAcceptance=" + processOverdueAcceptance +
                "}";
    }

    /**
     * This method generates a component that represents the recipe.
     * <br>
     * It only shows the ID but extends a Hover Event that shows the process time and the materials.
     * <br>
     * It also extends a Click Event that executes the command to show the recipe in the info command.
     * <br>
     * For use see {@link de.darkfinst.drugsadder.commands.ListCommand}
     *
     * @return The component that represents the recipe.
     */
    @Override
    public @NotNull Component asListComponent() {
        Component component = super.asListComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommandString(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.BARREL.getArg(), this.getRecipeID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * Returns the component that is used for the Hover Event of the recipe
     * <br>
     * It shows the process time and the materials.
     *
     * @return the component
     */
    @Override
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ProcessTime", this.getProcessTime() + ""));
        hover = hover.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ProcessOverdueAcceptance", this.getProcessOverdueAcceptance() + ""));
        hover = hover.appendNewline().append(super.getMaterialsAsComponent());
        return hover;
    }
}
