package exoticatechnologies.ui2.impl.resources

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet

open class ResourcesPanel(context: ResourcePanelContext) : ExoticaPanel(context) {
    private val handlers: MutableList<ResourceStringHandler> = mutableListOf()

    init {
        handlers.add(BandwidthResourceHandler())
        handlers.add(CreditsResourceHandler())
        handlers.add(StoryPointsResourceHandler())
        handlers.add(SpecialItemResourceHandler())
        handlers.add(StringResourceHandler())
        handlers.add(MissingResourceHandler())
    }

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)
        tooltip.addTitle(StringUtils.getString("CommonOptions", "CostTitle"))

        safeLet(member, variant, mods) { member, variant, mods ->
            context as ResourcePanelContext

            context.resourceCosts[Commodities.CREDITS] ?: run {
                context.resourceCosts[Commodities.CREDITS] = 0f
            }

            context.resourceCosts.forEach { (id, cost) ->
                val handler = handlers.firstOrNull { it.isHandlerFor(id) } ?: CommodityResourceHandler()

                handler.handle(tooltip, id, cost, mods, market)
            }
        }

        menuPanel.addUIElement(tooltip).inTL(0f, 0f)
    }
}

open class ResourcePanelContext : ExoticaPanelContext() {
    val resourceCosts: MutableMap<String, Float> = mutableMapOf()
}

interface ResourceStringHandler {
    fun isHandlerFor(resource: String): Boolean
    fun handle(tooltip: TooltipMakerAPI, resource: String, value: Float, mods: ShipModifications, market: MarketAPI?)
}