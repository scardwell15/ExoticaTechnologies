package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import data.scripts.util.MagicUI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.AIUtils
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sin

class PenanceEngine(key: String, settingsObj: JSONObject) :
    Exotic(key, settingsObj) {
    override var color = Color(0x00000)
        get() = Global.getSector().getFaction(Factions.LUDDIC_PATH).color

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
                .format("weaponRepairRate", WEAPON_REPAIRRATE_BUFF * getPositiveMult(member, mods, exoticData))
                .format("buffRange", getBuffRange(member, mods, exoticData))
                .formatFloat("armorRegenPerSec", ARMOR_REGEN_PER_SECOND)
                .format("armorRegenMax", ARMOR_REGEN_MAX)
                .format("sideSpeedBoost", getSideSpeedBoost(member, mods, exoticData))
                .format("damageThreshold", getDamageThreshold(member, mods, exoticData))
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
        stats.combatWeaponRepairTimeMult.modifyMult(buffId, 1 - (WEAPON_REPAIRRATE_BUFF * getPositiveMult(member, mods, exoticData)) / 100f)
    }

    override fun applyExoticToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        ship.setShield(ShieldAPI.ShieldType.NONE, 0f, 0f, 0f)
    }

    private val statusBarText: String
        get() = StringUtils.getString(key, "statusBarText")

    override fun advanceInCombatAlways(
        ship: ShipAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        var color = RenderUtils.getAliveUIColor()
        if (AIUtils.getNearbyEnemies(ship, getBuffRange(member, mods, exoticData)).isNotEmpty()) {
            val scalar = abs(sin(Global.getCombatEngine().getTotalElapsedTime(true) * 3))
            color = Color((180f + 70f * scalar) / 255f, 250f / 255f, (50f + 200f * scalar) / 255f)
        }

        val damage = getDamageTracker(ship).damage
        MagicUI.drawInterfaceStatusBar(
            ship,
            damage / getDamageThreshold(member, mods, exoticData),
            color,
            color,
            0f,
            statusBarText,
            -1
        )
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        if (ship.fluxTracker.isOverloaded) return

        val damage = getDamageTracker(ship).damage
        if (damage > getDamageThreshold(member, mods, exoticData)) {
            getDamageTracker(ship).damage = 0f

            for (i in 0..10) {
                Global.getCombatEngine().spawnEmpArcPierceShields(
                    ship,
                    ship.location,
                    null,
                    ship,
                    DamageType.OTHER,
                    0f,
                    200f,  // emp
                    10000f,  // max range
                    null,
                    20f,  // thickness
                    Color(100,165,255,255),
                    Color(255, 255, 255, 255)
                )
            }

            ship.fluxTracker.beginOverloadWithTotalBaseDuration(0.5f)
        } else {
            getDamageTracker(ship).damage = damage * (1f - amount / 5f)
        }

        val nearby = AIUtils.getNearbyEnemies(ship, getBuffRange(member, mods, exoticData))
        if (nearby.isNotEmpty()) {
            ship.mutableStats.acceleration.modifyPercent(buffId, 50f)
            ship.mutableStats.deceleration.modifyPercent(buffId, -50f)

            ship.engineController.fadeToOtherColor(buffId, Color(255, 120, 170), null, 1f, 0.75f)
            val velocityDir = VectorUtils.getFacing(ship.velocity) - ship.facing
            if (abs(velocityDir) > 60f) {
                ship.mutableStats.maxSpeed.modifyPercent(buffId, getSideSpeedBoost(member, mods, exoticData))
            } else {
                ship.mutableStats.maxSpeed.unmodify(buffId)
            }

            val interval = getRegenInterval(ship)
            interval.advance(amount)
            if (interval.intervalElapsed()) {
                val grid = ship.armorGrid.grid
                val max = ship.armorGrid.maxArmorInCell

                // Iterate through all armor cells and find any that aren't at max
                for (x in grid.indices) {
                    for (y in grid[0].indices) {
                        val armor = grid[x][y]
                        if (armor < max) {
                            val recoverPercentPerSecond = (nearby.size * ARMOR_REGEN_PER_SECOND)
                                .coerceAtMost(ARMOR_REGEN_MAX)
                            ship.armorGrid.setArmorValue(x, y, armor + max * recoverPercentPerSecond * interval.intervalDuration)
                        }
                    }
                }
            }
        } else {
            ship.mutableStats.acceleration.unmodify(buffId)
            ship.mutableStats.deceleration.unmodify(buffId)
            ship.engineController.fadeToOtherColor(buffId, Color(255, 120, 170), null, 0f, 0.75f)
        }
    }

    private fun getDamageTracker(ship: ShipAPI): PenanceEngineDamageTracker {
        var tracker = ship.customData["etPenanceTracker"]
        if (tracker == null) {
            tracker = PenanceEngineDamageTracker(ship)
            ship.setCustomData("etPenanceTracker", tracker)
            ship.addListener(tracker)
        }
        return tracker as PenanceEngineDamageTracker
    }

    private fun getRegenInterval(ship: ShipAPI): IntervalUtil {
        var interval = ship.customData["etPenanceRegenInterval"]
        if (interval == null) {
            interval = IntervalUtil(0.1f, 0.1f)
            ship.setCustomData("etPenanceRegenInterval", interval)
        }
        return interval as IntervalUtil
    }

    // damage listener
    private inner class PenanceEngineDamageTracker(private val ship: ShipAPI) : DamageListener {
        var damage = 0f

        override fun reportDamageApplied(source: Any?, target: CombatEntityAPI, result: ApplyDamageResultAPI) {
            if (target === ship && !ship.fluxTracker.isOverloaded) {
                val damageToShip = result.damageToHull + result.totalDamageToArmor;
                if (damageToShip > 0) {
                    damage += damageToShip
                }
            }
        }
    }

    fun getBuffRange(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData): Float {
        return BUFF_RANGE / getNegativeMult(member, mods, exoticData).coerceAtLeast(1f)
    }

    fun getSideSpeedBoost(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData): Float {
        return SIDE_SPEED_BOOST * getPositiveMult(member, mods, exoticData)
    }

    fun getDamageThreshold(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData): Float {
        val baseHull = member.hullSpec.hitpoints
        val modifiedHull = baseHull + member.stats.hullBonus.computeEffective(member.hullSpec.hitpoints)

        val baseArmor = member.hullSpec.armorRating
        val modifiedArmor = baseArmor + member.stats.armorBonus.computeEffective(member.hullSpec.armorRating)

        return (modifiedHull * 0.125f).coerceAtMost(modifiedArmor * 1.75f) / getNegativeMult(member, mods, exoticData)
    }

    companion object {
        private const val BUFF_RANGE = 1200
        private const val WEAPON_REPAIRRATE_BUFF = 60
        private const val SIDE_SPEED_BOOST = 40
        private const val ARMOR_REGEN_PER_SECOND = 2.5f
        private const val ARMOR_REGEN_MAX = 10f
    }
}