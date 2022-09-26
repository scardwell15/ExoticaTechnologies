package exoticatechnologies.ui.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ShipListUIPanelPlugin(var parentPanel: CustomPanelAPI) : BaseUIPanelPlugin() {
    private val rowSize = 64f
    private val pad = 3f
    private val opad = 10f
    private val textWidth = 240f

    private var listeners: MutableList<Listener> = mutableListOf()

    override var panelWidth: Float = this.getListWidth()
    override var panelHeight: Float = Global.getSettings().screenHeight * 0.66f

    var memberPanelMap: HashMap<FleetMemberAPI, ShipUIPanelPlugin> = hashMapOf()

    override fun renderBelow(alphaMult: Float) {
        RenderUtils.pushUIRenderingStack()

        //debug box
        RenderUtils.renderBox(pos.x, pos.y, pos.width, pos.height, Color.RED, 0.1F)

        //separator line
        RenderUtils.renderBox(pos.x + pos.width - 2f, pos.y + pos.height * 0.1f, 2f, pos.height * 0.8f, Global.getSettings().basePlayerColor, 0.125F)
        RenderUtils.renderBox(pos.x + pos.width - 2f, pos.y + pos.height * 0.2f, 2f, pos.height * 0.6f, Global.getSettings().basePlayerColor, 0.25F)

        RenderUtils.popUIRenderingStack()
    }

    private fun getListWidth() : Float {
        return textWidth + rowSize
    }

    private fun getListHeight(rows: Int) : Float {
        return opad * 2 + (rowSize + pad) * rows
    }

    fun layoutPanels(members: MutableList<FleetMemberAPI>): CustomPanelAPI {
        val outerPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val outerTooltip = outerPanel.createUIElement(panelWidth, panelHeight, false)

        val headerStr = StringUtils.getTranslation("FleetScanner", "NotableShipsHeader").toString()
        outerTooltip.addSectionHeading(headerStr, Alignment.MID, 0f)
        val heading = outerTooltip.prev

        val innerPanel = outerPanel.createCustomPanel(panelWidth, panelHeight, null)
        val tooltip: TooltipMakerAPI = innerPanel.createUIElement(panelWidth, panelHeight, true)

        members.forEach {
            val rowPlugin = createPanel(tooltip, it)
            memberPanelMap[it] = rowPlugin
        }

        innerPanel.addUIElement(tooltip).inTL(0f, 0f)
        outerTooltip.addCustom(innerPanel, 3f).position.belowMid(heading, 3f)
        outerPanel.addUIElement(outerTooltip).inTL(0f, 0f)
        parentPanel.addComponent(outerPanel).inTL(0f, 0f)

        return outerPanel
    }

    private fun createPanel(tooltip: TooltipMakerAPI, member: FleetMemberAPI): ShipUIPanelPlugin {
        val rowPlugin = ShipUIPanelPlugin(member, ShipModFactory.getForFleetMember(member))
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    private fun pickedMember(member: FleetMemberAPI, mods: ShipModifications) {
        listeners.forEach {
            it(member, mods)
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    inner class ShipUIPanelPlugin(var member: FleetMemberAPI, var mods: ShipModifications) : BaseUIPanelPlugin() {
        override var panelWidth: Float = this@ShipListUIPanelPlugin.getListWidth()
        override var panelHeight: Float = this@ShipListUIPanelPlugin.rowSize
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
                ?.let { this@ShipListUIPanelPlugin.pickedMember(member, mods) }
        }

        fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
            val shipNameColor = member.captain.faction.baseUIColor

            val panel: CustomPanelAPI =
                parentPanel.createCustomPanel(panelWidth, rowSize, this)

            // Ship image with tooltip of the ship class
            val shipImg = panel.createUIElement(iconSize, iconSize, false)
            shipImg.addShipList(1, 1, iconSize, Misc.getBasePlayerColor(), mutableListOf(member), 0f)
            panel.addUIElement(shipImg).inTL(0f, 0f)

            // Ship name, class, bandwidth
            val shipText = panel.createUIElement(textWidth, panelHeight, false)
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
}

typealias Listener = (member: FleetMemberAPI, mods: ShipModifications) -> Unit