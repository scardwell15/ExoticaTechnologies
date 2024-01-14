package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.combat.particles.ParticleController
import exoticatechnologies.combat.particles.ParticleData
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import org.magiclib.util.MagicUI
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.sin

class PenanceEngine(key: String, settingsObj: JSONObject) :
    Exotic(key, settingsObj) {
    override var color = Color(0x00000)
        get() = Global.getSector().getFaction(Factions.LUDDIC_PATH).color

    override fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI?): Boolean {
        return (Utilities.hasExoticChip(Global.getSector().playerFleet.cargo, key)
                || Utilities.hasExoticChip(Misc.getStorageCargo(market), key))
    }

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
                .format("weaponRepairRate", getRepairRateBuff(member, mods, exoticData))
                .format("buffRange", getBuffRange(member, mods, exoticData))
                .formatFloat("armorRegenPerSec", ARMOR_REGEN_PER_SECOND)
                .format("armorRegenMax", ARMOR_REGEN_MAX)
                .format("armorRegenPerSecondMax", ARMOR_REGEN_PER_SECOND_MAX)
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
        stats.combatWeaponRepairTimeMult.modifyMult(
            buffId,
            1 - getRepairRateBuff(member, mods, exoticData) / 100f
        )
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        if (ship.hullSpec.hullId == KADUR_CALIPH_SHIELD_GEN_ID) {
            ship.hitpoints = 0f
        }

        if (ship.hullSpec.hullId != KADUR_CALIPH_SHIELD_PART_ID) {
            ship.setShield(ShieldAPI.ShieldType.NONE, 0f, 0f, 0f)
        }
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

        if (ship.hullSpec.hullId == KADUR_CALIPH_SHIELD_GEN_ID) {
            ship.hitpoints = 0f
        }

        if (ship.hullSpec.hullId == KADUR_CALIPH_SHIELD_PART_ID) {
            ship.shield?.arc = 0f
        }

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
                    1000f,  // emp
                    10000f,  // max range
                    null,
                    20f,  // thickness
                    Color(100, 165, 255, 255),
                    Color(255, 255, 255, 255)
                )
            }

            ship.fluxTracker.beginOverloadWithTotalBaseDuration(2f)
        } else {
            getDamageTracker(ship).damage = damage * (1f - amount / 8f)
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
                var regened = 0f
                var maxRegen = ARMOR_REGEN_PER_SECOND_MAX * interval.intervalDuration
                val grid = ship.armorGrid.grid
                val shipArmorRating = ship.armorGrid.maxArmorInCell

                // Iterate through all armor cells and find any that aren't at max
                for (x in grid.indices) {
                    for (y in grid[0].indices) {
                        val armor = grid[x][y]
                        if (armor < shipArmorRating) {
                            val recoverPercentPerSecond = (nearby.size * ARMOR_REGEN_PER_SECOND)
                                .coerceAtMost(ARMOR_REGEN_MAX) / 100f
                            val recovered = (shipArmorRating * recoverPercentPerSecond * interval.intervalDuration)
                                .coerceAtMost(maxRegen)
                            ship.armorGrid.setArmorValue(
                                x,
                                y,
                                armor + recovered
                            )

                            regened += recovered
                            if (regened >= maxRegen) break
                        }
                    }
                }

                if (regened > 0) {
                    for (i in 0..(regened / 25f).toInt().coerceAtLeast(1)) {
                        val randomX = ship.collisionRadius * MathUtils.getRandomNumberInRange(-0.5f, 0.5f)
                        val randomY = ship.collisionRadius * MathUtils.getRandomNumberInRange(-0.5f, 0.5f)
                        ParticleController.INSTANCE.addParticle(
                            PenanceEngineParticleData(
                                x = ship.location.x + randomX,
                                y = ship.location.y + randomY,
                                xVel = ship.velocity.x * MathUtils.getRandomNumberInRange(-0.5f, 0.5f),
                                yVel = ship.velocity.y * MathUtils.getRandomNumberInRange(-0.5f, 0.5f),
                                angle = MathUtils.getRandomNumberInRange(0f, 360f),
                                aVel = MathUtils.getRandomNumberInRange(0f, 100f),
                                startingSize = 20f,
                                endSize = 5f,
                                ttl = 1f,
                                startingColor = color,
                                endColor = Color.BLACK.setAlpha(0)
                            )
                        )
                    }
                }
            }
        } else {
            ship.mutableStats.maxSpeed.unmodify(buffId)
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
        exoticData: ExoticData
    ): Float {
        return BUFF_RANGE / getNegativeMult(member, mods, exoticData).coerceAtLeast(1f)
    }

    fun getSideSpeedBoost(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ): Float {
        return SIDE_SPEED_BOOST * getPositiveMult(member, mods, exoticData)
    }

    fun getRepairRateBuff(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ): Float {
        return (WEAPON_REPAIRRATE_BUFF * getPositiveMult(member, mods, exoticData)).coerceAtMost(99f)
    }

    fun getDamageThreshold(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ): Float {
        val baseHull = member.hullSpec.hitpoints
        val modifiedHull = baseHull + member.stats.hullBonus.computeEffective(member.hullSpec.hitpoints)

        val baseArmor = member.hullSpec.armorRating
        val modifiedArmor = baseArmor + member.stats.armorBonus.computeEffective(member.hullSpec.armorRating)

        var thresh = modifiedHull * 0.125f
        if (thresh < modifiedArmor * 1.75f) {
            thresh = modifiedArmor * 1.75f
        }

        return (thresh / getNegativeMult(member, mods, exoticData)).coerceAtLeast(100f)
    }

    companion object {
        private const val BUFF_RANGE = 1200
        private const val WEAPON_REPAIRRATE_BUFF = 60
        private const val SIDE_SPEED_BOOST = 40
        private const val ARMOR_REGEN_PER_SECOND = 2.5f
        private const val ARMOR_REGEN_MAX = 10f
        private const val ARMOR_REGEN_PER_SECOND_MAX = 300f

        private const val KADUR_CALIPH_SHIELD_GEN_ID = "vayra_caliph_shieldgenerator"
        private const val KADUR_CALIPH_SHIELD_PART_ID = "vayra_caliph_shieldpart"
    }
}

class PenanceEngineParticleData(x: Float, y: Float, xVel: Float, yVel: Float, angle: Float, aVel: Float, ttl: Float, startingSize: Float, endSize: Float, startingColor: Color, endColor: Color)
    : ParticleData(
    sprite = Global.getSettings().getSprite("graphics/fx/cleaner_clouds00.png"),
    x = x,
    y = y,
    xVel = xVel,
    yVel = yVel,
    angle = angle,
    aVel = aVel,
    startingTime = Global.getCombatEngine().getTotalElapsedTime(false),
    ttl = ttl,
    startingSize = startingSize,
    endSize = endSize,
    startingColor = startingColor,
    endColor = endColor,
    spritesInRow = 2,
    spritesInColumn = 2)