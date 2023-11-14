package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DAShapedRecipe extends DARecipe {
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
     * Returns the materials of the recipe as a component
     *
     * @param hover     The hover of the component
     * @param shapeKeys The shape keys of the recipe
     * @return The materials of the recipe as a component
     */
    //TODO: Make Translatable
    public Component getMaterialsAsComponent(Component hover, Map<String, DAItem> shapeKeys) {
        hover = hover.appendNewline().append(Component.text("Materials:"));
        AtomicReference<Component> aHover = new AtomicReference<>(hover);
        shapeKeys.forEach((shapeKey, material) -> {
            Component name = material.getName();
            if (name == null) {
                name = Component.text(material.getItemStack().getType().name());
            }
            aHover.set(aHover.get().appendNewline().append(Component.text(shapeKey + ": ").append(Component.text(material.getAmount() + "x" + " ")).append(name)));
        });
        hover = aHover.get();
        return hover;
    }
}
