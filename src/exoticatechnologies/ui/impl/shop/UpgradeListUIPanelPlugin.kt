package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeListUIPanelPlugin(parentPanel: CustomPanelAPI,
                                var member: FleetMemberAPI,
                                var mods: ShipModifications): ListUIPanelPlugin<Upgrade>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "OpenUpgradeOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 100)

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Upgrade): ListItemUIPanelPlugin<Upgrade> {
        val rowPlugin = UpgradeItemUIPlugin(item, member, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowSize
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }
}