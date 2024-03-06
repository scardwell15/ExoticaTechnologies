package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.crafting.ingredients.spec.IngredientSpec
import exoticatechnologies.ui2.list.ListItem
import exoticatechnologies.ui2.list.ListItemContext
import exoticatechnologies.ui2.list.ListPanelContext

class InputIngredientSelector(context: InputIngredientSelectorContext) :
    ListItem<Ingredient>(context) {
    val ingredient: Ingredient
        get() = (currContext as InputIngredientSelectorContext).item
    val ingredientSpec: IngredientSpec<out Ingredient>
        get() = (currContext as InputIngredientSelectorContext).recipeIngredientPanelContext.ingredientSpec

    override fun decorate(menuPanel: CustomPanelAPI) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)
        ingredientSpec.decorateSelector(tooltip, ingredient, ingredient.getQuantity())
        tooltip.heightSoFar = panelHeight
        menuPanel.addUIElement(tooltip).inMid()
    }
}

open class InputIngredientSelectorContext(
    ingredient: Ingredient,
    val recipeIngredientPanelContext: RecipeIngredientPanelContext
) :
    ListItemContext<Ingredient>(ingredient) {
    override fun createListItem(listContext: ListPanelContext<Ingredient>): ListItem<Ingredient> {
        return InputIngredientSelector(this)
    }

    override fun isActive(): Boolean {
        return recipeIngredientPanelContext.isIngredientSelected(item)
    }
}
