package exoticatechnologies.ui.impl.ships

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import java.awt.Color

class ShipListUIPanelPlugin(parentPanel: CustomPanelAPI): ListUIPanelPlugin<FleetMemberAPI>(parentPanel) {

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: FleetMemberAPI): ListItemUIPanelPlugin<FleetMemberAPI> {
        val rowPlugin = ShipItemUIPanelPlugin(item, ShipModFactory.getForFleetMember(item), this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowSize
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun renderBelow(alphaMult: Float) {
        RenderUtils.pushUIRenderingStack()

        //debug box
        RenderUtils.renderBox(pos.x, pos.y, pos.width, pos.height, Color.RED, 0.1F)

        //separator line
        RenderUtils.renderBox(pos.x + pos.width - 2f, pos.y + pos.height * 0.1f, 2f, pos.height * 0.8f, Global.getSettings().basePlayerColor, 0.125F)
        RenderUtils.renderBox(pos.x + pos.width - 2f, pos.y + pos.height * 0.2f, 2f, pos.height * 0.6f, Global.getSettings().basePlayerColor, 0.25F)

        RenderUtils.popUIRenderingStack()
    }
}