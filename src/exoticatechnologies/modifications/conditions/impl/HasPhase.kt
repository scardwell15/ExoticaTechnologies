package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class HasPhase: OperatorCondition() {
    override val key = "hasPhase"
    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return member.variant.hullSpec.shieldType == ShieldAPI.ShieldType.PHASE
                || member.isPhaseShip
                || (member.variant.hullSpec.shipSystemId != null
                && Global.getSettings().getShipSystemSpec(member.variant.hullSpec.shipSystemId) != null
                && Global.getSettings().getShipSystemSpec(member.variant.hullSpec.shipSystemId).isPhaseCloak)
    }
}