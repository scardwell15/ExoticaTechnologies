package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class MinimumCrew : OperatorCondition() {
    override val key = "minimumCrew"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        return member.hullSpec.minCrew
    }
}