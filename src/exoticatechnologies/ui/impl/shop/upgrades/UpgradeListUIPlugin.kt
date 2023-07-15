package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getMods
import java.awt.Color

class UpgradeListUIPlugin(parentPanel: CustomPanelAPI,
                          var member: FleetMemberAPI,
                          var market: MarketAPI?
): ListUIPanelPlugin<Upgrade>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "OpenUpgradeOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Upgrade): ListItemUIPanelPlugin<Upgrade> {
        val rowPlugin = UpgradeListItemUIPlugin(item, member, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<Upgrade>): List<Upgrade> {
        val mods = member.getMods()
        return items.sortedWith { a, b ->
            if (mods.hasUpgrade(a))
                if (mods.hasUpgrade(b))
                    mods.getUpgrade(a) - mods.getUpgrade(b)
                else
                    -1
            else
                if (mods.hasUpgrade(b))
                    1
                else if (a.canApply(member, mods))
                    if (b.canApply(member, mods))
                        0
                    else
                        -1
                else if (b.canApply(member, mods))
                    1
            else
                0
        }
    }

    override fun shouldMakePanelForItem(item: Upgrade): Boolean {
        val mods = member.getMods()

        if (mods.hasUpgrade(item)) {
            return true
        }

        if (market == null) {
            return false
        }
        
        return item.shouldShow(member, mods, market)
    }
}