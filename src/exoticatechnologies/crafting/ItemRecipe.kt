package exoticatechnologies.crafting

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import exoticatechnologies.cargo.CrateItemDialog
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.ui2.impl.crafting.RecipeOutputPreviewPanelContext

abstract class ItemRecipe : Recipe {
    abstract fun getOutputItemStack(ingredients: List<List<Ingredient>>): CargoStackAPI?

    override fun crafted(
        fleet: CampaignFleetAPI,
        market: MarketAPI?,
        ingredients: List<List<Ingredient>>,
        recipeOutputPreviewPanelContext: RecipeOutputPreviewPanelContext?
    ): Any? {
        val newStack: CargoStackAPI? = getOutputItemStack(ingredients)
        if (newStack != null) {
            fleet.cargo.addFromStack(newStack)
        }
        return newStack
    }
}