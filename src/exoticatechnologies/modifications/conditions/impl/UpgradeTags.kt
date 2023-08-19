package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition
import exoticatechnologies.modifications.upgrades.ETUpgrades

class UpgradeTags : OperatorCondition() {
    override val key = "upgradetags"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        return (mods?.upgrades ?: ETUpgrades()).tags
    }
}