package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.impl.shop.ResourcesUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.methods.ExoticMethod

class ExoticResourcesUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    member: FleetMemberAPI,
    variant: ShipVariantAPI,
    mods: ShipModifications,
    market: MarketAPI?,
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

    fun redisplayResourceCosts(activeMethod: ExoticMethod?) {
        destroyTooltip()

        //gather resource costs first, across all upgrade methods
        val resourceCosts: MutableMap<String, Float> = linkedMapOf()

        if (activeMethod != null) {
            if (market == null && !activeMethod.canUseIfMarketIsNull()) {
                resourceCosts["^CommonOptions.MustBeDockedAtMarket"] = 1f
            } else {
                activeMethod.getResourceMap(member, mods, exotic, market, true)?.forEach { (key, cost) ->
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
}