package de.darkfinst.drugsadder.recipe;

public enum RecipeType {

    BARREL,
    CRAFTING,
    PRESS,
    TABLE,
    FURNACE,
    SMOKING,
    BLASTING,
    CRAFTER,
    ;

    public static String getNamedRecipeID(RecipeType recipeType, String recipeID) {
        return recipeType.name().toLowerCase() + ":" + recipeID;
    }
}
