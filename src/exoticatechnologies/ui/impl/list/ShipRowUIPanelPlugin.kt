package exoticatechnologies.ui.impl.list

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ShipUIPanelPlugin(var member: FleetMemberAPI, var mods: ShipModifications, private val listPanel: ShipListUIPanelPlugin) : BaseUIPanelPlugin() {
    private val pad = 3f
    private val opad = 10f
    override var panelWidth: Float = 304f
    override var panelHeight: Float = 64f
    var bgColor: Color = Color(0, 0, 0, 0)

    override fun renderBelow(alphaMult: Float) {
        if (bgColor.alpha > 0) {
            RenderUtils.pushUIRenderingStack()
            RenderUtils.renderBox(pos.x, pos.y, pos.width, pos.height, bgColor, bgColor.alpha / 255f)
            RenderUtils.popUIRenderingStack()
        }
    }

    override fun processInput(events: List<InputEventAPI>) {
        events
            .filter { it.isLMBUpEvent }
            .takeIf { it.any { event -> pos.containsEvent(event) } }
            ?.let { listPanel.pickedMember(member, mods) }
    }

    fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val shipNameColor = member.captain.faction.baseUIColor

        val panel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        // Ship image with tooltip of the ship class
        val shipImg = panel.createUIElement(iconSize, iconSize, false)
        shipImg.addShipList(1, 1, iconSize, Misc.getBasePlayerColor(), mutableListOf(member), 0f)
        panel.addUIElement(shipImg).inTL(0f, 0f)

        // Ship name, class, bandwidth
        val shipText = panel.createUIElement(panelWidth - iconSize, panelHeight, false)
        shipText.addPara(member.shipName, shipNameColor, 0f)
        shipText.addPara(member.hullSpec.nameWithDesignationWithDashClass, 0f)

        val bandwidth = mods.getBandwidthWithExotics(member)
        StringUtils.getTranslation("FleetScanner", "ShipBandwidthShort")
            .format(
                "bandwidth",
                BandwidthUtil.getFormattedBandwidthWithName(bandwidth),
                Bandwidth.getBandwidthColor(bandwidth)
            )
            .addToTooltip(shipText, pad)
        panel.addUIElement(shipText).rightOfTop(shipImg, pad)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(panel, opad)

        return panel
    }
}