package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition
import exoticatechnologies.modifications.exotics.ETExotics

class Exotics : OperatorCondition() {
    override val key = "exotics"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return mods?.exotics ?: ETExotics()
    }
}