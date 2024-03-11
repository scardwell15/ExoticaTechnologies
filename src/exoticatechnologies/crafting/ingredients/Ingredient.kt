package exoticatechnologies.crafting.ingredients

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.getRoundedValue

interface Ingredient {
    /**
     * Consumes ingredient.
     * @return amount of ingredient still required
     */
    fun take(fleet: CampaignFleetAPI, market: MarketAPI?, amount: Float): Float
    fun getQuantity(): Float

    fun getQuantityText(): String {
        return StringUtils.getTranslation("Recipes", "QuantityText").format("quantity", getQuantity().getRoundedValue()).toStringNoFormats()
    }
}