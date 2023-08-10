package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class HasPhase : OperatorCondition() {
    override val key = "hasPhase"
    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return member.hullSpec.hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE)
    }
}