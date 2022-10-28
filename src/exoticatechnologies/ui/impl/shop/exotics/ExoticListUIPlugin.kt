package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticListUIPlugin(parentPanel: CustomPanelAPI,
                         var member: FleetMemberAPI,
                         var mods: ShipModifications): ListUIPanelPlugin<Exotic>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("ExoticsDialog", "OpenExoticOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Exotic): ListItemUIPanelPlugin<Exotic> {
        val rowPlugin = ExoticItemUIPlugin(item, member, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun shouldMakePanelForItem(item: Exotic): Boolean {
        if (mods.hasExotic(item)) {
            return true
        }

        return item.canApply(member)
    }
}