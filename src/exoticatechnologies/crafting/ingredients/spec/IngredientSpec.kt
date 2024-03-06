package exoticatechnologies.crafting.ingredients.spec

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.crafting.ingredients.Ingredient
import org.magiclib.kotlin.getRoundedValue

interface IngredientSpec<T : Ingredient> {
    fun getRequired(): Float
    fun hasRequiredAmount(ingredients: List<Ingredient>): Boolean
    fun createIngredients(fleet: CampaignFleetAPI, market: MarketAPI?): List<T>

    fun sortIngredients(ingredients: List<Ingredient>): List<Ingredient> {
        return ingredients
    }

    fun canUseIngredientForStack(ingredient: Ingredient): Boolean {
        return true
    }

    fun getIconSprite(ingredient: T?): String

    /**
     * @param selectedIngredients list of selected ingredients. can be empty. is empty if selector list is open.
     */
    fun getIconSprite(selectedIngredients: List<Ingredient>): String {
        return getIconSprite(selectedIngredients.firstOrNull() as T?)
    }

    fun getItemName(ingredient: T?): String

    /**
     * @param selectedIngredients list of selected ingredients. can be empty. is empty if selector list is open.
     */
    fun getItemName(selectedIngredients: List<Ingredient>): String {
        return getItemName(selectedIngredients.firstOrNull() as T?)
    }

    fun decorateRecipeIngredient(tooltip: TooltipMakerAPI, selectedIngredients: List<Ingredient>, selectorOpen: Boolean) {
        val ingredients = if (selectorOpen) mutableListOf() else selectedIngredients
        tooltip.addImage(getIconSprite(ingredients), 64f, 0f)
        val sprite = tooltip.prev
        sprite.position.inLMid(4f)

        tooltip.addPara(getItemName(ingredients), 0f).position.rightOfMid(sprite, 4f).setYAlignOffset(16f)
        val name = tooltip.prev
        tooltip.addPara("Required: ${ getRequired().getRoundedValue() }", 0f).position.belowLeft(name, 2f)
        val required = tooltip.prev
        tooltip.addPara("Selected: ${ ingredients.sumOf{ it.getQuantity().toDouble() }.toFloat().getRoundedValue() }", 0f).position.belowLeft(required, 2f)
    }

    fun decorateSelector(tooltip: TooltipMakerAPI, ingredient: Ingredient, quantity: Float) {
        ingredient as T
        tooltip.addImage(getIconSprite(ingredient), 64f,0f)
        val sprite = tooltip.prev
        sprite.position.inLMid(4f)

        tooltip.addPara(getItemName(ingredient), 0f).position.rightOfMid(sprite, 4f).setYAlignOffset(8f)
        val name = tooltip.prev
        tooltip.addPara(ingredient.getQuantityText(), 0f).position.belowLeft(name, 2f)
    }

    fun getFiltersForIngredients(selectedIngredients: List<Ingredient>): List<String> {
        return listOf()
    }

    fun getFiltersForIngredient(ingredient: Ingredient): List<String> {
        return listOf()
    }
}

