package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class CargoSpace : OperatorCondition() {
    override val key = "cargoSpace"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return member.cargoCapacity
    }
}