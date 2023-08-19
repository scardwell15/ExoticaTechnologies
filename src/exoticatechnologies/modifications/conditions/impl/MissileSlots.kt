package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

class MissileSlots : OperatorCondition() {
    override val key = "missileSlots"

    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI): Any? {
        return member.hullSpec.allWeaponSlotsCopy
            .filter { !it.isSystemSlot && !it.isStationModule && !it.isDecorative }
            .count {
                it.weaponType.equals(WeaponAPI.WeaponType.MISSILE)
                    || it.weaponType.equals(WeaponAPI.WeaponType.COMPOSITE)
                    || it.weaponType.equals(WeaponAPI.WeaponType.SYNERGY)
                    || it.weaponType.equals(WeaponAPI.WeaponType.UNIVERSAL)
            }
    }
}