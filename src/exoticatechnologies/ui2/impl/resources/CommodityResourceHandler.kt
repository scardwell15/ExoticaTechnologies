package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class CommodityResourceHandler : ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return true
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        //commodities
        val name = Utilities.getItemName(resource)
        val quantity = Utilities.getTotalQuantity(Global.getSector().playerFleet, market, resource).toFloat()

        if (value != 0f) {
            StringUtils.getTranslation("CommonOptions", "ResourceTextWithCost")
                .format("name", name)
                .format("amount", Misc.getWithDGS(quantity))
                .format("cost", StringUtils.formatCost(value))
                .addToTooltip(tooltip)
        } else {
            StringUtils.getTranslation("CommonOptions", "ResourceText")
                .format("name", name)
                .format("amount", Misc.getWithDGS(quantity))
                .addToTooltip(tooltip)
        }
    }
}