package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.impl.shop.chips.ChipListUIPlugin
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeChipListUIPlugin(
    parentPanel: CustomPanelAPI,
    member: FleetMemberAPI
) : ChipListUIPlugin(parentPanel, member) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "UpgradeChipsHeader").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(
        tooltip: TooltipMakerAPI,
        item: CargoStackAPI
    ): ListItemUIPanelPlugin<CargoStackAPI> {
        val rowPlugin = UpgradeChipListItemUIPlugin(item, member, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }
}