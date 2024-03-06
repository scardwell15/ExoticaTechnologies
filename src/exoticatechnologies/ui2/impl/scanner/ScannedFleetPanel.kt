package exoticatechnologies.ui2.impl.scanner

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.ui2.list.ListPanel
import exoticatechnologies.ui2.list.ListPanelContext

open class ScannedFleetPanel(context: ScannedFleetPanelContext) :
    ListPanel<FleetMemberAPI>(context) {
}

open class ScannedFleetPanelContext(scannedMembers: List<FleetMemberAPI>) :
    ListPanelContext<FleetMemberAPI>() {
    init {
        scannedMembers.forEach {
            listItems.add(ScannedMemberContext(it, ShipModFactory.generateForFleetMember(it)))
        }
    }
}