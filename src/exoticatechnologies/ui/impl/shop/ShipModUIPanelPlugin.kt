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
import kotlin.math.max

class ShipModUIPanelPlugin(var parentPanel: CustomPanelAPI, override var panelWidth: Float, override var panelHeight: Float) : BaseUIPanelPlugin() {
    private val pad = 3f
    private val opad = 10f

    private var innerPanel: CustomPanelAPI
    private var innerTooltip: TooltipMakerAPI

    init {
        val outerPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val outerTooltip = outerPanel.createUIElement(panelWidth, panelHeight, false)

        val headerStr = StringUtils.getTranslation("MainMenu", "ShipModMenu").toString()
        outerTooltip.addSectionHeading(headerStr, Alignment.MID, 0f)
        val heading = outerTooltip.prev

        innerPanel = outerPanel.createCustomPanel(panelWidth, panelHeight - 16, null)

        innerTooltip = showNothing()

        outerTooltip.addCustom(innerPanel, 3f).position.belowMid(heading, 3f)
        outerPanel.addUIElement(outerTooltip).inTL(0f, 0f)
        parentPanel.addComponent(outerPanel).inTR(0f, 0f)
    }

    override fun renderBelow(alphaMult: Float) {
        RenderUtils.pushUIRenderingStack()
        RenderUtils.renderBox(pos.x, pos.y, pos.width, pos.height, Color.BLUE, 0.1F)
        RenderUtils.popUIRenderingStack()
    }

    fun showPanel(member: FleetMemberAPI?): CustomPanelAPI {
        innerPanel.removeComponent(innerTooltip)
        if (member != null) {
            innerTooltip = showMember(member)
        } else {
            innerTooltip = showNothing()
        }
        return innerPanel
    }

    private fun showNothing(): TooltipMakerAPI {
        val tooltip = innerPanel.createUIElement(panelWidth, panelHeight - 16, false)

        innerPanel.addUIElement(tooltip).inTL(0f, 0f)

        return tooltip
    }

    private fun showMember(member: FleetMemberAPI): TooltipMakerAPI {
        val tooltip = innerPanel.createUIElement(panelWidth, panelHeight - 16, false)

        val rowPlugin = ShipHeaderUIPanelPlugin(member, ShipModFactory.getForFleetMember(member))
        rowPlugin.layoutPanel(tooltip)

        innerPanel.addUIElement(tooltip).inTL(0f, 0f)

        return tooltip
    }

    private inner class ShipHeaderUIPanelPlugin(var member: FleetMemberAPI, var mods: ShipModifications) : BaseUIPanelPlugin() {
        override var panelWidth: Float = this@ShipModUIPanelPlugin.panelWidth
        override var panelHeight: Float = max(this@ShipModUIPanelPlugin.panelHeight * 0.1f, Global.getSettings().screenHeight * 0.166f)

        override fun processInput(events: List<InputEventAPI>) {
        }

        fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
            val shipNameColor = member.captain.faction.baseUIColor

            val panel: CustomPanelAPI =
                parentPanel.createCustomPanel(panelWidth, panelHeight, this)

            // Ship image with tooltip of the ship class
            val shipImg = panel.createUIElement(iconSize, iconSize, false)
            shipImg.addShipList(1, 1, iconSize, Misc.getBasePlayerColor(), mutableListOf(member), 0f)
            panel.addUIElement(shipImg).inTL(0f, 0f)

            // Ship name, class, bandwidth
            val shipText = panel.createUIElement(panelWidth, panelHeight, false)
            shipText.setParaOrbitronLarge()
            shipText.addPara("${member.shipName} (${member.hullSpec.nameWithDesignationWithDashClass})", shipNameColor, 0f)
            shipText.setParaFontDefault()

            val baseBandwidth = mods.getBaseBandwidth(member)
            StringUtils.getTranslation("CommonOptions", "BaseBandwidthForShip")
                .format(
                    "shipBandwidth",
                    BandwidthUtil.getFormattedBandwidthWithName(baseBandwidth),
                    Bandwidth.getBandwidthColor(baseBandwidth)
                )
                .addToTooltip(shipText, pad)

            val exoticBandwidth = mods.getBandwidthWithExotics(member)
            if (baseBandwidth != exoticBandwidth) {
                StringUtils.getTranslation("CommonOptions", "ExoticBandwidthForShip")
                    .format(
                        "shipBandwidth",
                        BandwidthUtil.getFormattedBandwidthWithName(exoticBandwidth),
                        Bandwidth.getBandwidthColor(exoticBandwidth)
                    )
                    .addToTooltip(shipText, pad)
            }

            panel.addUIElement(shipText).rightOfTop(shipImg, pad)

            // done, add row to TooltipMakerAPI
            tooltip.addCustom(panel, opad)

            return panel
        }
    }
}