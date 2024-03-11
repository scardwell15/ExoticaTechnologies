package exoticatechnologies.crafting.ingredients

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import exoticatechnologies.crafting.ingredients.spec.IngredientSource
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.getRoundedValue
import org.magiclib.kotlin.ucFirst
import kotlin.math.min

open class CargoMaterial(val stack: CargoStackAPI, val source: IngredientSource) : Ingredient {
    override fun take(fleet: CampaignFleetAPI, market: MarketAPI?, amount: Float): Float {
        val amountToTake = min(stack.size, amount)
        stack.subtract(amountToTake)
        if (stack.size == 0f) {
            stack.cargo.removeStack(stack)
        }
        return amount - amountToTake
    }

    override fun getQuantity(): Float {
        return stack.size
    }

    override fun getQuantityText(): String {
        return StringUtils.getTranslation("Recipes", "QuantityInCargoText")
            .format("quantity", getQuantity().getRoundedValue())
            .format("cargo", source.name.lowercase().ucFirst())
            .toStringNoFormats()
    }

    override fun equals(other: Any?): Boolean {
        if (other is CargoMaterial) {
            return other.stack == this.stack
        }
        return super.equals(other)
    }
}