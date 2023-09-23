package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class HasPhase : OperatorCondition() {
    override val key = "hasPhase"
    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        return member.hullSpec.hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE)
                || variant.hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE)
                || member.hullSpec.isPhase
                || (member.variant.hullSpec.shipSystemId != null
                && Global.getSettings().getShipSystemSpec(member.variant.hullSpec.shipSystemId)?.isPhaseCloak == true)
                || (member.variant.hullSpec.shipDefenseId != null
                && Global.getSettings().getShipSystemSpec(member.variant.hullSpec.shipDefenseId)?.isPhaseCloak == true)
    }
}