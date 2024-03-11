package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.util.StringUtils
import kotlin.math.absoluteValue

class BandwidthResourceHandler: ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return resource == Bandwidth.BANDWIDTH_RESOURCE
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        val used = mods.getUsedBandwidth()
        if (value != 0f) {
            val bandwidthString = "+${BandwidthUtil.getFormattedBandwidth(value.absoluteValue)}"
            if (value > 0f) {
                StringUtils.getTranslation("Bandwidth", "BandwidthUsedWithCost")
                    .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                    .format("upgradeBandwidth", bandwidthString)
                    .addToTooltip(tooltip)
            } else if (value < 0) {
                StringUtils.getTranslation("Bandwidth", "BandwidthGiven")
                    .format("bandwidth", BandwidthUtil.getFormattedBandwidth(used))
                    .format("exoticBandwidth", bandwidthString)
                    .addToTooltip(tooltip)
            }
        } else {
            StringUtils.getTranslation("Bandwidth", "BandwidthUsed")
                .format("usedBandwidth", BandwidthUtil.getFormattedBandwidth(used))
                .addToTooltip(tooltip)
        }
    }
}