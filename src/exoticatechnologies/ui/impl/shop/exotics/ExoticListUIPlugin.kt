package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.filtered.FilteredListPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticListUIPlugin(
    parentPanel: CustomPanelAPI,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI?
) : FilteredListPanelPlugin<Exotic>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("ExoticsDialog", "OpenExoticOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)
    private var modsValue: Float = mods.getValue()

    override fun advancePanel(amount: Float) {
        val newValue = mods.getValue()
        if (modsValue != newValue) {
            modsValue = newValue
            layoutPanels()
        }
    }

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Exotic): ListItemUIPanelPlugin<Exotic> {
        val rowPlugin = ExoticItemUIPlugin(item, member, variant, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<Exotic>): List<Exotic> {
        val sortedItems = items.sortedWith { a, b ->
            if (mods.hasExotic(a))
                if (mods.hasExotic(b))
                    0
                else
                    -1
            else if (mods.hasExotic(b))
                1
            else
                if (a.canAfford(Global.getSector().playerFleet, market))
                    if (b.canAfford(Global.getSector().playerFleet, market))
                        0
                    else
                        -1
                else if (b.canAfford(Global.getSector().playerFleet, market))
                    1
                else
                    if (a.canApply(member, mods))
                        if (b.canApply(member, mods))
                            0
                        else
                            -1
                    else if (b.canApply(member, mods))
                        1
                    else
                        0
        }
        return sortedItems
    }

    override fun shouldMakePanelForItem(item: Exotic): Boolean {
        if (!super.shouldMakePanelForItem(item)) return false

        if (mods.hasExotic(item)) {
            return true
        }

        if (member.shipName == null && !item.shouldAffectModule(null, null)) return false

        if (market == null) {
            return false
        }

        return item.shouldShow(member, mods, market)
    }

    override fun getFilters(): List<String>? {
        return ExoticsHandler.EXOTICS_BY_HINT.keys.filterNot { it.isEmpty() }.toList()
    }

    override fun getFiltersFromItem(item: Exotic): List<String> {
        return item.hints
    }
}