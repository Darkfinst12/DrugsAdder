package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public abstract class DAShapedRecipe extends DARecipe {

    /**
     * Whether the recipe is shapeless or not
     */
    @Setter
    private boolean isShapeless = false;

    /**
     * The shape of the recipe
     * <br>
     * The shape is a list of strings with a length of three or five, depending on the recipe type
     * <br>
     * Each string represents a row of the shape
     * <br>
     * Each character represents a slot in the row
     */
    private final List<String> shape = new ArrayList<>();

    /**
     * A list of keys for the shape, these keys are used to match the materials
     */
    private final Map<String, DAItem> shapeKeys = new HashMap<>();

    /**
     * Creates a new recipe that is shaped
     *
     * @param ID         The id of the recipe
     * @param recipeType The type of the recipe
     * @param result     The result of the recipe
     * @param materials  The materials of the recipe
     */
    protected DAShapedRecipe(String ID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(ID, recipeType, result, materials);
    }

    /**
     * Sets the shape of the recipe
     *
     * @param shape The shape of the recipe
     */
    public void setShape(String... shape) {
        this.shape.clear();
        this.shape.addAll(Arrays.asList(shape));
    }

    /**
     * Sets the shape keys of the recipe
     *
     * @param shapeKeys The shape keys of the recipe
     */
    public void setShapeKeys(@NotNull Map<String, DAItem> shapeKeys) {
        this.shapeKeys.clear();
        this.shapeKeys.putAll(shapeKeys);
    }

    /**
     * Returns the shape of the recipe as a component
     *
     * @param component The component to append the shape to
     * @return The shape of the recipe as a component
     */
    public @NotNull Component getShapeComponent(Component component) {
        component = component.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_IsShapeless", this.isShapeless + ""));
        if (!this.isShapeless) {
            component = component.append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Shape"));
            for (String row : this.shape) {
                component = component.appendNewline().append(Component.text(row));
            }
        }
        return component;
    }

    /**
     * Returns the materials of the recipe as a component
     *
     * @param component The component to append the materials to
     * @return The materials of the recipe as a component
     */
    public Component getMaterialsAsComponent(Component component) {
        component = component.append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Materials"));
        AtomicReference<Component> aHover = new AtomicReference<>(component);
        this.shapeKeys.forEach((shapeKey, material) -> {
            Component name = material.getName();
            if (name == null) {
                name = Component.text(material.getItemStack().getType().name());
            }
            aHover.set(aHover.get().appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ShapeKey", shapeKey).append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_AmountX", material.getAmount() + "").append(name))));
        });
        component = aHover.get();
        return component;
    }
}
