package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.ui2.list.ListItem
import exoticatechnologies.ui2.list.ListItemContext
import exoticatechnologies.ui2.list.ListPanelContext
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getFleetModuleSafe
import java.awt.Color


class ScannedMemberItem(context: ScannedMemberContext) :
    ListItem<FleetMemberAPI>(context) {
    val member: FleetMemberAPI
        get() = (currContext as ScannedMemberContext).item
    val mods: ShipModifications
        get() = (currContext as ScannedMemberContext).mods


    override fun decorate(menuPanel: CustomPanelAPI) {
        val factionColor: Color = (member.getFleetModuleSafe()?.faction ?: member.captain.faction).baseUIColor

        // Ship image with tooltip of the ship class
        val shipImageTooltip = menuPanel.createUIElement(64f, panelHeight, false)
        val memberAsList: MutableList<FleetMemberAPI> = ArrayList()
        memberAsList.add(member)
        shipImageTooltip.addShipList(1, 1, 64f, Misc.getBasePlayerColor(), memberAsList, 0f)
        menuPanel.addUIElement(shipImageTooltip)//.inMid()

        // Ship name, class, bandwidth
        val shipTextTooltip: TooltipMakerAPI = menuPanel.createUIElement(240f, 50f, false)
        shipTextTooltip.addPara(member.shipName, factionColor, 0f)
        shipTextTooltip.addPara(member.hullSpec.nameWithDesignationWithDashClass, 0f)

        val bandwidth = mods.getBaseBandwidth()
        StringUtils.getTranslation("FleetScanner", "ShipBandwidthShort")
            .format("bandwidth", BandwidthUtil.getFormattedBandwidthWithName(bandwidth), Bandwidth.getColor(bandwidth))
            .addToTooltip(shipTextTooltip, 0f)
        menuPanel.addUIElement(shipTextTooltip).rightOfMid(shipImageTooltip, innerPadding)

        val tabsContext = ScannedMemberModsContext(member, mods)
        val tabsPanel = ScannedMemberMods(tabsContext)
        tabsPanel.panelWidth = panelWidth - 240f - 64f - innerPadding * 3f
        tabsPanel.panelHeight = panelHeight
        tabsPanel.layoutPanel(menuPanel, null).position.rightOfMid(shipTextTooltip, innerPadding)
    }
}

open class ScannedMemberContext(member: FleetMemberAPI, val mods: ShipModifications) :
    ListItemContext<FleetMemberAPI>(member) {
    override val unselectedColor: Color
        get() = Color.BLACK
    override val highlightedColor: Color
        get() = Color.BLACK
    override val activeColor: Color
        get() = Color.BLACK
    override val activeHighlightedColor: Color
        get() = Color.BLACK

    override fun createListItem(listContext: ListPanelContext<FleetMemberAPI>): ListItem<FleetMemberAPI> {
        return ScannedMemberItem(this)
    }
}
