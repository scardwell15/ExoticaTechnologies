package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class SpecialItemResourceHandler: ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return Utilities.isSpecialItemId(resource)
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        val specialId = Utilities.getSpecialItemId(resource)
        val specialParams = Utilities.getSpecialItemParams(resource)

        val stack = Utilities.getSpecialStack(Global.getSector().playerFleet.cargo, specialId, specialParams)
        val name: String
        var quantity = 0f
        if (stack != null) {
            name = stack.displayName
            quantity = stack.size
        } else {
            val cargo = Global.getFactory().createCargo(true)
            val fakeData = SpecialItemData(specialId, specialParams)
            val fakeStack = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.SPECIAL, fakeData, cargo)
            name = fakeStack.displayName
        }

        if (value != 0f) {
            var translationKey = "SpecialItemTextWithCost"
            if (value < 0) {
                translationKey = "SpecialItemTextWithPay"
            }
            StringUtils.getTranslation("CommonOptions", translationKey)
                .format("name", name)
                .format("amount", Misc.getWithDGS(quantity))
                .format("cost", StringUtils.formatCost(value))
                .addToTooltip(tooltip)
        } else {
            var quantityText: String? = "-"
            if (quantity > 0) {
                quantityText = Misc.getWithDGS(quantity)
            }
            StringUtils.getTranslation("CommonOptions", "ResourceText")
                .format("name", name)
                .format("amount", quantityText)
                .addToTooltip(tooltip)
        }
    }
}