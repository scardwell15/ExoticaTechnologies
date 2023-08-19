package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class GlobalMemory: OperatorCondition() {
    override val key: String
        get() = "globalMemory"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        return Global.getSector().memoryWithoutUpdate[extra.toString()]
    }
}