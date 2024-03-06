package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils

class StringResourceHandler : ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return resource.startsWith("^")
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        val splitParentKey = resource.substring(1).split(".")
        StringUtils.getTranslation(splitParentKey[0], splitParentKey[1])
            .addToTooltip(tooltip)
    }
}