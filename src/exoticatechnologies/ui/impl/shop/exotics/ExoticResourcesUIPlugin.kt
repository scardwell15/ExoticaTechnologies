package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.impl.shop.ResourcesUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.methods.Method

class ExoticResourcesUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    member: FleetMemberAPI,
    mods: ShipModifications,
    var market: MarketAPI,
    var methods: List<Method>
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

    fun redisplayResourceCosts(activeMethod: Method?) {
        destroyTooltip()

        //gather resource costs first, across all upgrade methods
        val resourceCosts: MutableMap<String, Float> = linkedMapOf()

        methods.forEach { method ->
            val hovered = method == activeMethod
            method.getResourceMap(member, mods, exotic, market, hovered)?.forEach { (key, cost) ->
                resourceCosts.merge(key, cost, Float::plus)
            }
        }

        resourcesTooltip = displayResourceCosts(resourceCosts)
    }
}