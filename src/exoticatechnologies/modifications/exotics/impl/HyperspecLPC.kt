package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import java.awt.Color

class HyperspecLPC(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color: Color = Color(255, 230, 0)

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    ) {
        if (expand) {
            StringUtils.getTranslation(key, "longDescription")
                .formatFloat("fighterDamageIncrease", getDamageBonus(member, mods, exoticData))
                .formatFloat("fighterSpeedIncrease", getSpeedBonus(member, mods, exoticData))
                .formatFloat("fighterArmorIncrease", getArmorBonus(member, mods, exoticData))
                .formatFloat("fighterFluxIncrease", getFluxBonus(member, mods, exoticData))
                .formatFloat("replacementTimeIncrease", getReplacementTimeMalus(member, mods, exoticData))
                .addToTooltip(tooltip, title)
        }
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val fighterBays = member.hullSpec.fighterBays
        stats.numFighterBays.modifyFlat(buffId, -(fighterBays - 1f))
        stats.fighterRefitTimeMult.modifyPercent(buffId, getReplacementTimeMalus(member, mods, exoticData))
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        ship.mutableStats.numFighterBays.modifyFlat(buffId, -ship.mutableStats.numFighterBays.modifiedValue)
    }

    override fun applyToFighters(member: FleetMemberAPI, ship: ShipAPI, fighter: ShipAPI, mods: ShipModifications) {
        val exoticData = mods.getExoticData(this)!!

        fighter.mutableStats.ballisticWeaponDamageMult.modifyPercent(buffId, getDamageBonus(member, mods, exoticData))
        fighter.mutableStats.energyWeaponDamageMult.modifyPercent(buffId, getDamageBonus(member, mods, exoticData))

        fighter.mutableStats.maxSpeed.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))
        fighter.mutableStats.acceleration.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))
        fighter.mutableStats.deceleration.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))
        fighter.mutableStats.turnAcceleration.modifyPercent(buffId, getSpeedBonus(member, mods, exoticData))

        fighter.mutableStats.armorBonus.modifyPercent(buffId, getArmorBonus(member, mods, exoticData))
        fighter.mutableStats.fluxCapacity.modifyPercent(buffId, getFluxBonus(member, mods, exoticData))
        fighter.mutableStats.fluxDissipation.modifyPercent(buffId, getFluxBonus(member, mods, exoticData))
    }

    fun getBaysRemoved(member: FleetMemberAPI): Int {
        return (member.hullSpec.fighterBays - 1)
    }

    fun getDamageBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return FIGHTER_DAMAGE_MAX.coerceAtMost(
            FIGHTER_DAMAGE_PER_BAY + getBaysRemoved(member)
        )
    }

    fun getSpeedBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return FIGHTER_SPEED_MAX.coerceAtMost(
            FIGHTER_SPEED_PER_BAY + getBaysRemoved(member)
        )
    }


    fun getArmorBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return FIGHTER_ARMOR_MAX.coerceAtMost(
            FIGHTER_ARMOR_PER_BAY + getBaysRemoved(member)
        )
    }


    fun getFluxBonus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return FIGHTER_FLUX_MAX.coerceAtMost(
            FIGHTER_FLUX_PER_BAY + getBaysRemoved(member)
        )
    }

    fun getReplacementTimeMalus(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return REPLACEMENT_TIME_MAX.coerceAtMost(
            REPLACEMENT_TIME_PER_BAY + getBaysRemoved(member)
        )
    }

    companion object {
        private const val FIGHTER_DAMAGE_MAX = 90f
        private const val FIGHTER_ARMOR_MAX = 120f
        private const val FIGHTER_FLUX_MAX = 120f
        private const val FIGHTER_SPEED_MAX = 100f
        private const val REPLACEMENT_TIME_MAX = 60f

        private const val FIGHTER_DAMAGE_PER_BAY = 30f

        private const val FIGHTER_ARMOR_PER_BAY = 30f

        private const val FIGHTER_FLUX_PER_BAY = 30f

        private const val FIGHTER_SPEED_PER_BAY = 40f

        private const val REPLACEMENT_TIME_PER_BAY = 10f
    }
}