package exoticatechnologies.crafting.ingredients

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import kotlin.math.min

class CreditsIngredient(val credits: Float) : Ingredient {
    override fun take(fleet: CampaignFleetAPI, market: MarketAPI?, amount: Float): Float {
        val amountToTake = min(credits, amount)
        fleet.cargo.credits.subtract(amountToTake)
        return amount - amountToTake
    }

    override fun getQuantity(): Float {
        return credits
    }

    override fun equals(other: Any?): Boolean {
        if (other is CreditsIngredient) {
            return true
        }
        return super.equals(other)
    }
}