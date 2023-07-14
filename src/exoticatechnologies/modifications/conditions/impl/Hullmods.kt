package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class Hullmods : OperatorCondition() {
    override val key = "hullmods"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return member.variant.hullMods
    }
}