package exoticatechnologies.crafting

import exoticatechnologies.crafting.impl.CombineChipsRecipe
import exoticatechnologies.crafting.impl.ExoticTypeSwitchRecipe

object RecipeManager {
    private val recipes: MutableList<Recipe> = mutableListOf()
    private val recipesByHint: MutableMap<String, MutableSet<Recipe>> = mutableMapOf()

    @JvmStatic
    fun getRecipes(): List<Recipe> {
        return recipes
    }

    @JvmStatic
    fun getRecipesByHint(): Map<String, Set<Recipe>> {
        return recipesByHint
    }

    @JvmStatic
    fun initialize() {
        recipes.clear()
        recipesByHint.clear()

        loadRecipes()
    }

    @JvmStatic
    fun loadRecipes() {
        addRecipe(CombineChipsRecipe())
        addRecipe(ExoticTypeSwitchRecipe())
    }

    @JvmStatic
    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
        recipe.getHints().forEach {
            recipesByHint.getOrPut(it) { LinkedHashSet() }
                .add(recipe)
        }
    }
}