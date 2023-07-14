package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class MissileSlots : OperatorCondition() {
    override val key = "missileSlots"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        return member.variant.fittedWeaponSlots
            .map { member.variant.getSlot(it) }
            .filter { !it.isSystemSlot && !it.isStationModule && !it.isDecorative }
            .count {it.weaponType.equals(WeaponAPI.WeaponType.MISSILE)
                    || it.weaponType.equals(WeaponAPI.WeaponType.COMPOSITE)
                    || it.weaponType.equals(WeaponAPI.WeaponType.SYNERGY)
                    || it.weaponType.equals(WeaponAPI.WeaponType.UNIVERSAL)
            }
    }
}