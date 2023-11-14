package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DAShapedRecipe extends DARecipe {
    protected DAShapedRecipe(String ID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(ID, recipeType, result, materials);
    }

    public Component getMaterials(Component hover, Map<String, DAItem> shapeKeys) {
        hover = hover.append(Component.text("\nMaterials:"));
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
