package exoticatechnologies.ui.impl.shop.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import java.awt.Color

abstract class ChipListUIPlugin(parentPanel: CustomPanelAPI,
                       var member: FleetMemberAPI): ListUIPanelPlugin<CargoStackAPI>(parentPanel) {
    override var bgColor: Color = Color(255, 70, 255, 0)
    var hoveredPlugin: ChipListItemUIPlugin? = null

    override fun advancePanel(amount: Float) {
        hoveredPlugin = null
        panelPluginMap.values
            .filterIsInstance(ChipListItemUIPlugin::class.java)
            .forEach {
                if (it.wasHovered) {
                    hoveredPlugin = it
                }
            }
    }

    override fun createListHeader(tooltip: TooltipMakerAPI) {
        tooltip.addTitle(listHeader).position.inTMid(0f)
    }
}