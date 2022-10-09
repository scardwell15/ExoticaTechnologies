package exoticatechnologies.ui.impl.ships

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ShipItemUIPanelPlugin(item: FleetMemberAPI, var mods: ShipModifications, private val listPanel: ListUIPanelPlugin<FleetMemberAPI>
) : ListItemUIPanelPlugin<FleetMemberAPI>(item) {
    private val pad = 3f
    private val opad = 10f
    override var panelWidth: Float = 304f
    override var panelHeight: Float = 64f
    override var bgColor: Color = Color(200, 200, 200, 0)
    var lastBandwidth: Float = -1f
    var bandwidthText: LabelAPI? = null

    override fun renderBelow(alphaMult: Float) {
        if (bgColor.alpha > 0) {
            RenderUtils.pushUIRenderingStack()
            RenderUtils.renderBox(pos.x, pos.y, pos.width - 10f, pos.height, bgColor, bgColor.alpha / 255f)
            RenderUtils.popUIRenderingStack()
        }
    }

    override fun advance(amount: Float) {
        val newBandwidth = mods.getBandwidthWithExotics(item)
        if (newBandwidth != lastBandwidth) {
            setBandwidthText(newBandwidth)
        }
    }

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val shipNameColor = item.captain.faction.baseUIColor

        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        // Ship image with tooltip of the ship class
        val shipImg = rowPanel.createUIElement(iconSize, iconSize, false)
        shipImg.addShipList(1, 1, iconSize, Misc.getBasePlayerColor(), mutableListOf(item), 0f)
        rowPanel.addUIElement(shipImg).inTL(0f, 0f)

        // Ship name, class, bandwidth
        val shipText = rowPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
        shipText.addPara(item.shipName, shipNameColor, 0f)
        shipText.addPara(item.hullSpec.nameWithDesignationWithDashClass, 0f)

        val bandwidth = mods.getBandwidthWithExotics(item)
        bandwidthText = shipText.addPara("", Misc.getTextColor(), 0f)
        setBandwidthText(bandwidth)

        rowPanel.addUIElement(shipText).rightOfTop(shipImg, pad)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, opad)

        panel = rowPanel

        return panel!!
    }

    private fun setBandwidthText(bandwidth: Float) {
        lastBandwidth = bandwidth
        bandwidthText?.let {
            it.text = StringUtils.getTranslation("FleetScanner", "ShipBandwidthShort")
                .format(
                    "bandwidth",
                    BandwidthUtil.getFormattedBandwidthWithName(bandwidth)
                ).toStringNoFormats()

            it.setHighlightColor(Bandwidth.getBandwidthColor(bandwidth))
            it.setHighlight(BandwidthUtil.getFormattedBandwidthWithName(bandwidth))
        }
    }
}