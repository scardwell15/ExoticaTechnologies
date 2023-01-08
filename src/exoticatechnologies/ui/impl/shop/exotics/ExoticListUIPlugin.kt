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

    override fun shouldMakePanelForItem(item: Exotic): Boolean {
        val mods = member.getMods()
        if (mods.hasExotic(item)) {
            return true
        }

        return item.shouldShow(member, mods, market!!)
    }
}