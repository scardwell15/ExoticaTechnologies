package exoticatechnologies.ui2.impl.crafting

import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.ui2.list.FilteredListContext
import exoticatechnologies.ui2.list.FilteredListPanel

open class InputIngredientSelectionList(context: InputIngredientSelectionListContext) :
    FilteredListPanel<Ingredient>(context) {

    val ingredientSelectionContext: InputIngredientSelectionListContext
        get() = currContext as InputIngredientSelectionListContext

    override fun getFilters(): List<String>? {
        return ingredientSelectionContext.recipeIngredientPanelContext.ingredientSpec.getFiltersForIngredients(ingredientSelectionContext.createdIngredients)
    }

    override fun getFiltersFromItem(item: Ingredient): List<String> {
        return ingredientSelectionContext.recipeIngredientPanelContext.ingredientSpec.getFiltersForIngredient(item)
    }
}

open class InputIngredientSelectionListContext(
    val recipeIngredientPanelContext: RecipeIngredientPanelContext,
    showSelectedOnly: Boolean
) :
    FilteredListContext<Ingredient>() {

    val createdIngredients = recipeIngredientPanelContext.createIngredients()
    init {
        if (showSelectedOnly) {
            recipeIngredientPanelContext.selectedIngredients
                .forEach {
                    listItems.add(InputIngredientSelectorContext(it, recipeIngredientPanelContext))
                }
        } else {
            createdIngredients
                .forEach {
                    listItems.add(InputIngredientSelectorContext(it, recipeIngredientPanelContext))
                }
        }
    }

    override fun sortItems(items: List<Ingredient>): List<Ingredient> {
        return recipeIngredientPanelContext.ingredientSpec.sortIngredients(items)
    }
}