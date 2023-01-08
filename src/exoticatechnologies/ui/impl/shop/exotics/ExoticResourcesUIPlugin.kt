package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.impl.shop.ResourcesUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.methods.Method
import exoticatechnologies.util.getMods

class ExoticResourcesUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    member: FleetMemberAPI,
    var market: MarketAPI,
    var methods: List<Method>
) : ResourcesUIPlugin(member) {
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
        val mods = member.getMods()
        destroyTooltip()

        //gather resource costs first, across all upgrade methods
        val resourceCosts: MutableMap<String, Float> = linkedMapOf()

        methods.forEach { method ->
            val hovered = method == activeMethod
            method.getResourceMap(member, mods, exotic, market, hovered)?.forEach { (key, cost) ->
                if (resourceCosts[key] != null) {
                    resourceCosts[key] = resourceCosts[key]!!.plus(cost)
                } else {
                    resourceCosts[key] = cost
                }
            }
        }

        resourcesTooltip = displayResourceCosts(resourceCosts)
    }
}