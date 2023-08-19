package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class Faction: OperatorCondition() {
    override val key = "faction"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        var faction: String? = null
        if (member.hullId.contains("ziggurat")) {
            faction = "omega"
        } else if (member.fleetData != null && member.fleetData.fleet != null) {
            faction = member.fleetData.fleet.faction.id
        }

        return faction ?: ""
    }
}