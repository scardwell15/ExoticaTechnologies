package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.impl.shop.upgrades.methods.UpgradeMethod
import exoticatechnologies.ui.impl.shop.ResourcesUIPlugin

class UpgradeResourcesUIPlugin(
    var parentPanel: CustomPanelAPI,
    var upgrade: Upgrade,
    member: FleetMemberAPI,
    mods: ShipModifications,
    var market: MarketAPI
) : ResourcesUIPlugin(member, mods) {
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

        //gather resource costs first, across all upgrade methods
        val resourceCosts: MutableMap<String, Float> = linkedMapOf()

        resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = upgrade.bandwidthUsage
        UpgradesHandler.UPGRADE_METHODS.forEach { method ->
            val hovered = method == activeMethod
            method?.getResourceCostMap(member, mods, upgrade, market, hovered)?.forEach { (key, cost) ->
                resourceCosts.merge(key, cost, Float::plus)
            }
        }

        resourcesTooltip = displayResourceCosts(resourceCosts)
    }
}