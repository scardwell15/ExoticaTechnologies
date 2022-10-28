package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import java.awt.Color

class UpgradeListItemUIPlugin(
    item: Upgrade,
    var member: FleetMemberAPI,
    var mods: ShipModifications,
    private val listPanel: ListUIPanelPlugin<Upgrade>
) : ListItemUIPanelPlugin<Upgrade>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    private val opad = 6f
    override var panelWidth: Float = 222f
    override var panelHeight: Float = 64f

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        // Ship image with tooltip of the ship class
        val itemInfo = rowPanel.createUIElement(panelWidth - 6f, panelHeight, false)
        itemInfo.addImage(item.icon, iconSize, 16f)
        val image = itemInfo.prev
        itemInfo.addPara(item.name, 0f).position.rightOfMid(image, 3f)
        rowPanel.addUIElement(itemInfo).inLMid(0f)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, 0f)

        panel = rowPanel

        return panel!!
    }
}