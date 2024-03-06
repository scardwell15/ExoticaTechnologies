package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class MissingResourceHandler : ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return Utilities.isResourceString(resource)
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        val resourceName = resource.substring(1)
        val quantityText = "0"
        if (value != 0f) {
            var translationKey = "SpecialItemTextWithCost"
            if (value < 0) {
                translationKey = "SpecialItemTextWithPay"
            }
            StringUtils.getTranslation("CommonOptions", translationKey)
                .format("name", resourceName)
                .format("amount", quantityText)
                .format("cost", StringUtils.formatCost(value))
                .addToTooltip(tooltip)
        } else {
            StringUtils.getTranslation("CommonOptions", "ResourceText")
                .format("name", resourceName)
                .format("amount", quantityText)
                .addToTooltip(tooltip)
        }
    }
}