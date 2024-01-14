package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.impl.shop.ResourcesUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod

class UpgradeResourcesUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    member: FleetMemberAPI,
    variant: ShipVariantAPI,
    mods: ShipModifications,
    market: MarketAPI?
) : ResourcesUIPlugin(member, variant, mods, market) {
    override var mainPanel: CustomPanelAPI? = null
    private var resourcesTooltip: TooltipMakerAPI? = null

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        redisplayResourceCosts(null)

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    fun destroyTooltip() {
        resourcesTooltip?.let {
            mainPanel!!.removeComponent(it)
        }
    }

    fun redisplayResourceCosts(activeMethod: UpgradeMethod?) {
        destroyTooltip()

        val resourceCosts: MutableMap<String, Float> = linkedMapOf()
        resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = upgrade.bandwidthUsage
        resourceCosts[Commodities.CREDITS] = 0f

        if (activeMethod != null) {
            if (market == null && !activeMethod.canUseIfMarketIsNull()) {
                resourceCosts["^CommonOptions.MustBeDockedAtMarket"] = 1f
            } else {
                activeMethod.getResourceCostMap(member, mods, upgrade, market, true).forEach { (key, cost) ->
                    if (resourceCosts[key] != null) {
                        resourceCosts[key] = resourceCosts[key]!!.plus(cost)
                    } else {
                        resourceCosts[key] = cost
                    }
                }
            }
        }

        resourcesTooltip = displayResourceCosts(resourceCosts)
    }

    companion object {
        const val CANNOT_USE = "##cannotuse##"
    }
}