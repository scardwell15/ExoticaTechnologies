package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getMods
import java.awt.Color

class ExoticListUIPlugin(parentPanel: CustomPanelAPI,
                         var member: FleetMemberAPI,
                         var market: MarketAPI?): ListUIPanelPlugin<Exotic>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("ExoticsDialog", "OpenExoticOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Exotic): ListItemUIPanelPlugin<Exotic> {
        val rowPlugin = ExoticItemUIPlugin(item, member, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<Exotic>): List<Exotic> {
        val mods = member.getMods()
        val sortedItems = items.sortedWith { a, b ->
            if (mods.hasExotic(a))
                if (mods.hasExotic(b))
                    0
                else
                    -1
            else if (mods.hasExotic(b))
                    1
            else
                if (a.canAfford(member.fleetData.fleet, market))
                    if (b.canAfford(member.fleetData.fleet, market))
                        0
                    else
                        -1
                else if (b.canAfford(member.fleetData.fleet, market))
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
        val mods = member.getMods()
        if (mods.hasExotic(item)) {
            return true
        }

        if (market == null) {
            return false
        }

        return item.shouldShow(member, mods, market)
    }
}