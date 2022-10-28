package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ChipListUIPlugin(parentPanel: CustomPanelAPI,
                       var member: FleetMemberAPI,
                       var mods: ShipModifications): ListUIPanelPlugin<CargoStackAPI>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "UpgradeChipsHeader").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: CargoStackAPI): ListItemUIPanelPlugin<CargoStackAPI> {
        val rowPlugin = ChipListItemUIPlugin(item, member, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun createListHeader(tooltip: TooltipMakerAPI) {
        tooltip.addTitle(listHeader).position.inTMid(0f)
    }
}