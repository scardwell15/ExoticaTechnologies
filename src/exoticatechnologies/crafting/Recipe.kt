package exoticatechnologies.crafting

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.crafting.ingredients.spec.IngredientSpec
import exoticatechnologies.ui2.impl.crafting.RecipeResultPanel
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanel
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanelContext
import java.awt.Color

interface Recipe {
    fun getName(): String
    fun getHints(): List<String>
    fun getDescription(): String
    fun getListItemColor(): Color = Color.BLACK
    fun getTextColor(): Color = Misc.getBasePlayerColor()

    fun canShow(): Boolean {
        return true
    }

    fun canUse(): Boolean {
        return true
    }

    fun requiresMarket(): Boolean {
        return true
    }

    fun crafted(
        fleet: CampaignFleetAPI,
        market: MarketAPI?,
        ingredients: List<List<Ingredient>>,
        recipeOutputPreviewPanelContext: RecipeOutputPreviewPanelContext?
    ): Any?

    fun takeIngredients(fleet: CampaignFleetAPI, market: MarketAPI?, ingredients: List<List<Ingredient>>) {
        val specs = getIngredientSpecs(ingredients)
        specs.forEachIndexed { index, spec ->
            val ingredientsForSpec = ingredients[index]
            var required = spec.getRequired()
            ingredientsForSpec.forEach {
                required -= it.take(fleet, market, required)
            }
        }
    }

    fun postCraft(
        recipeOutputPreviewPanelContext: RecipeOutputPreviewPanelContext,
        recipeOutputPreviewPanel: RecipeOutputPreviewPanel,
        craftObject: Any?
    )

    fun modifyPostCraftingOutputTooltip(
        recipeResultPanel: RecipeResultPanel,
        tooltip: TooltipMakerAPI,
        panelWidth: Float,
        panelHeight: Float,
        craftingOutputData: Any?
    )

    fun modifyOutputPreviewTooltip(
        tooltip: TooltipMakerAPI,
        panelWidth: Float,
        panelHeight: Float,
        pickedIngredients: List<List<Ingredient>>
    )

    fun getIngredientSpecs(pickedIngredients: List<List<Ingredient>>?): List<IngredientSpec<out Ingredient>>

    fun canCraft(pickedIngredients: List<List<Ingredient>>): Boolean {
        getIngredientSpecs(pickedIngredients).forEachIndexed { index, ingredientSpec ->
            if (!ingredientSpec.hasRequiredAmount(pickedIngredients[index])) {
                return false
            }
        }
        return true
    }

    fun canUseIngredient(index: Int, ingredientSpec: IngredientSpec<out Ingredient>, ingredient: Ingredient): Boolean
}