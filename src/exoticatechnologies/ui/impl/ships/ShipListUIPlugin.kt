package exoticatechnologies.ui.impl.ships

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils

class ShipListUIPlugin(parentPanel: CustomPanelAPI) : ListUIPanelPlugin<FleetMemberAPI>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("ShipListDialog", "ShipListHeader").toString()

    override fun createPanelForItem(
        tooltip: TooltipMakerAPI,
        item: FleetMemberAPI
    ): ListItemUIPanelPlugin<FleetMemberAPI> {
        val rowPlugin = ShipItemUIPlugin(item, ShipModFactory.generateForFleetMember(item), this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun renderBelow(alphaMult: Float) {
        RenderUtils.pushUIRenderingStack()

        //separator line
        RenderUtils.renderBox(
            pos.x + pos.width - 2f,
            pos.y + pos.height * 0.1f,
            2f,
            pos.height * 0.8f,
            Global.getSettings().basePlayerColor,
            0.125F
        )
        RenderUtils.renderBox(
            pos.x + pos.width - 2f,
            pos.y + pos.height * 0.2f,
            2f,
            pos.height * 0.6f,
            Global.getSettings().basePlayerColor,
            0.25F
        )

        RenderUtils.popUIRenderingStack()
    }

    fun modsModified(member: FleetMemberAPI, mods: ShipModifications) {
        panelPluginMap.filter { it.key == member}
            .values.forEach {
                if (it is ShipItemUIPlugin) {
                    it.updateWithNewMods(mods)
                }
            }
    }
}