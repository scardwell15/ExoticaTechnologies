package exoticatechnologies.campaign.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoPickerListener
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.util.StringUtils

object MarketMenu {
    fun open(market: MarketAPI, dialog: InteractionDialogAPI) {
        val marketData = MarketManager.getDataForMarket(market)

        dialog.showCargoPickerDialog(
            "Exotica Market",
            StringUtils.getString("Options", "confirm"),
            StringUtils.getString("Options", "cancel"),
            false,
            400f,
            marketData.cargo,
            MarketCargoListener(dialog, marketData)
        )
    }
}

class MarketCargoListener(val dialog: InteractionDialogAPI, val marketData: MarketData) : CargoPickerListener {
    val faction
        get() = marketData.faction

    override fun pickedCargo(cargo: CargoAPI) {
        val bounty: Float = computeCreditValue(cargo)
        if (Global.getSector().playerFleet.cargo.credits.get() < bounty) return

        marketData.cargo.removeAll(cargo)
        Global.getSector().playerFleet.cargo.addAll(cargo)
        Global.getSector().playerFleet.cargo.credits.subtract(bounty)

        StringUtils.getTranslation("MarketMenu", "PurchasedText")
            .format("credits", Misc.getWithDGS(bounty) + Strings.C)
            .addToTextPanel(dialog.textPanel)
    }

    override fun cancelledCargoSelection() {
        //donothing
    }

    override fun recreateTextPanel(
        panel: TooltipMakerAPI,
        cargo: CargoAPI,
        pickedUp: CargoStackAPI?,
        pickedUpFromSource: Boolean,
        combined: CargoAPI
    ) {
        panel.setParaFontOrbitron()
        panel.addPara(Misc.ucFirst(faction.displayName), faction.baseUIColor, 1f)
        panel.setParaFontDefault()

        panel.addImage(faction.logo, 310 * 1f, 3f)

        val playerCredits = Global.getSector().playerFleet.cargo.credits.get()
        StringUtils.getTranslation("MarketMenu", "CreditsText")
            .format("credits", Misc.getWithDGS(playerCredits) + Strings.C)
            .addToTooltip(panel)

        val purchasePrice: Float = computeCreditValue(combined)
        val canAfford = playerCredits >= purchasePrice

        if (canAfford) {
            StringUtils.getTranslation("MarketMenu", "MenuText")
                .format("credits", Misc.getWithDGS(purchasePrice) + Strings.C)
                .addToTooltip(panel)
        } else {
            StringUtils.getTranslation("MarketMenu", "MenuTextCannotAfford")
                .format("credits", Misc.getWithDGS(purchasePrice) + Strings.C)
                .addToTooltip(panel)
        }
    }

    protected fun computeCreditValue(cargo: CargoAPI): Float {
        var bounty = 0f
        for (stack in cargo.stacksCopy) {
            val plugin = stack.plugin
            if (plugin is UpgradeSpecialItemPlugin) {
                bounty += plugin.upgrade!!.getCreditCostForResources(plugin.upgradeLevel) * stack.size
            } else if (plugin is ExoticSpecialItemPlugin) {
                bounty += 250000 * stack.size
            }
        }
        return bounty
    }
}