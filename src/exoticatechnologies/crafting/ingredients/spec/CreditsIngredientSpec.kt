package exoticatechnologies.crafting.ingredients.spec

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import exoticatechnologies.crafting.ingredients.CreditsIngredient
import exoticatechnologies.crafting.ingredients.Ingredient

class CreditsIngredientSpec(private val reqCredits: Float): IngredientSpec<CreditsIngredient> {
    override fun getRequired(): Float {
        return reqCredits
    }

    override fun hasRequiredAmount(ingredients: List<Ingredient>): Boolean {
        return ingredients.sumOf { (it as CreditsIngredient).credits.toDouble() } > getRequired()
    }

    override fun createIngredients(fleet: CampaignFleetAPI, market: MarketAPI?): List<CreditsIngredient> {
        return listOf(CreditsIngredient(fleet.cargo.credits.get()))
    }

    override fun getIconSprite(ingredient: CreditsIngredient?): String {
        return "graphics/ui/icons/fleettab/credits_24x24.png"
    }

    override fun getItemName(ingredient: CreditsIngredient?): String {
        return Global.getSettings().getCommoditySpec(Commodities.CREDITS).name
    }
}