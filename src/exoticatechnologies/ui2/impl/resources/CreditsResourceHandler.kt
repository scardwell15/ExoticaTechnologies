package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.util.StringUtils

class CreditsResourceHandler : ResourceStringHandler {
    override fun isHandlerFor(resource: String): Boolean {
        return resource == Commodities.CREDITS
    }

    override fun handle(
        tooltip: TooltipMakerAPI,
        resource: String,
        value: Float,
        mods: ShipModifications,
        market: MarketAPI?
    ) {
        val name: String = Global.getSector().economy.getCommoditySpec(Commodities.CREDITS).name
        val credits: Float = Global.getSector().playerFleet.cargo.credits.get()

        if (value > 0) {
            StringUtils.getTranslation("UpgradesDialog", "ResourceTextWithCost")
                .format("name", name)
                .format("amount", credits)
                .format("cost", value * -1)
                .addToTooltip(tooltip)
        } else if (value < 0) {
            StringUtils.getTranslation("UpgradesDialog", "ResourceTextWithPay")
                .format("name", name)
                .format("amount", credits)
                .format("cost", value)
                .addToTooltip(tooltip)
        } else {
            StringUtils.getTranslation("UpgradesDialog", "ResourceText")
                .format("name", name)
                .format("amount", credits)
                .addToTooltip(tooltip)
        }
    }
}