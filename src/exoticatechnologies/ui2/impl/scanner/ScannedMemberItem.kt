package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.ui2.list.ListItem
import exoticatechnologies.ui2.list.ListItemContext
import exoticatechnologies.ui2.list.ListPanelContext

class ScannedMemberItem(context: ScannedMemberContext) :
    ListItem<FleetMemberAPI>(context) {
    val member: FleetMemberAPI
        get() = (currContext as ScannedMemberContext).item

    override fun decorate(menuPanel: CustomPanelAPI) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)
        
        tooltip.heightSoFar = panelHeight
        menuPanel.addUIElement(tooltip).inMid()
    }
}

open class ScannedMemberContext(member: FleetMemberAPI, val mods: ShipModifications) :
    ListItemContext<FleetMemberAPI>(member) {

    override fun createListItem(listContext: ListPanelContext<FleetMemberAPI>): ListItem<FleetMemberAPI> {
        return ScannedMemberItem(this)
    }
}
