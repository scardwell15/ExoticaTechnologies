package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.modifications.upgrades.methods.UpgradeMethod
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.impl.shop.ResourcesUIPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

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