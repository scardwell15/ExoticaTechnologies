package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class LaunchBays: OperatorCondition() {
    override val key = "launchBays"
    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return member.variant.wings?.size ?: 0
    }
}