package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

@Getter
public abstract class DARecipe {

    /**
     * The id of the recipe
     * <br>
     * The id is set in the config and is used to identify the recipe.
     * It is possible to give multiple recipes the same id if they have different types.
     */
    private final String recipeID;
    /**
     * The type of the recipe
     *
     * @see RecipeType
     */
    private final RecipeType recipeType;
    /**
     * The result of the recipe
     */
    private final DAItem result;
    /**
     * The materials of the recipe
     */
    private DAItem[] materials;

    protected DARecipe(String recipeID, RecipeType recipeType, DAItem result, DAItem... materials) {
        this.recipeID = recipeID;
        this.recipeType = recipeType;
        this.result = result;
        this.materials = materials;
    }

    /**
     * Checks if the given items are materials of the recipe
     *
     * @param givenItems The items to check
     * @return If the given items are materials of the recipe
     */
    public boolean hasMaterials(ItemStack... givenItems) {
        for (DAItem material : this.getMaterials()) {
            boolean contains = false;
            for (ItemStack item : givenItems) {
                if (DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return false;
            }
        }
        return true;
    }

    public void setMaterials(DAItem... materials) {
        this.materials = materials;
    }

    /**
     * Returns the named id of the recipe in the format of {@code <recipeType>:<namedID>}
     * <p>
     * Dose the same as {@link RecipeType#getNamedRecipeID(RecipeType, String)}
     *
     * @return The named id of the recipe
     */
    public String getRecipeNamedID() {
        return this.recipeType.name().toLowerCase() + ":" + this.recipeID;
    }

    /**
     * Returns the material of the given item
     *
     * @param item The item to get the material from
     * @return The material of the given item or null if the item is not a material
     */
    @Nullable
    public DAItem getMaterial(@NotNull ItemStack item) {
        return Arrays.stream(this.materials).filter(material -> DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes())).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "DARecipe{" +
                "namedID='" + recipeID + '\'' +
                ", recipeType=" + recipeType +
                ", materials=" + Arrays.toString(materials) +
                ", result=" + result +
                '}';
    }

    /**
     * This method generates a component that represents the recipe.
     * <br>
     * This may be overridden by the child class to add additional information.
     * <br>
     * It is used in the {@link de.darkfinst.drugsadder.commands.ListCommand}.
     *
     * @return The component that represents the recipe.
     */
    public @NotNull Component asListComponent() {
        return Component.text(RecipeType.getNamedRecipeID(this.recipeType, this.recipeID));
    }

    /**
     * This method generates a component that represents the recipe.
     * <br>
     * This may be overridden by the child class to add additional information.
     * <br>
     * It is used in the {@link de.darkfinst.drugsadder.commands.InfoCommand}.
     *
     * @return The component that represents the recipe.
     */
    public @NotNull Component asInfoComponent() {
        Component component = Component.text(this.recipeID);
        component = component.appendNewline().append(this.getHover());
        return component;
    }

    /**
     * This method generates a component used as a hover text for the recipe.
     * <p>
     * Note that this method is an abstract method and needs to be implemented by the child class.
     *
     * @return The hover text component.
     */
    public abstract @NotNull Component getHover();

    /**
     * This method generates a text component that lists all the materials used in the recipe.
     * Each material is listed on a new line with its quantity and name.
     * If the material does not have a name, the type of the item stack is used instead.
     *
     * @return A text component that represents the materials used in the recipe.
     */
    public @NotNull Component getMaterialsAsComponent() {
        Component hover = DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Materials");
        for (DAItem material : this.materials) {
            Component name = material.getName();
            if (name == null) {
                name = Component.text(material.getItemStack().getType().name());
            }
            hover = hover.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_AmountX", material.getAmount() + " ")).append(name);
        }
        return hover;
    }
}
