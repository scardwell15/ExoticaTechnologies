package exoticatechnologies.modifications.exotics.impl

import activators.ActivatorManager
import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import org.lazywizard.lazylib.CollectionUtils.SortEntitiesByDistance
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.combat.getNearestPointOnBounds
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.sin

class DriveFluxVent(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0x9D62C4)
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI): Boolean {
        return Utilities.hasItem(fleet.cargo, ITEM)
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI): Boolean {
        Utilities.takeItemQuantity(fleet.cargo, ITEM, 1f)
        return true
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
                .format("ventBonus", getDissipationIncrease(member, mods, exoticData))
                .format("fluxVentPercent", FLUX_VENTED)
                .format("fluxVentMax", FLUX_VENTED_MAX)
                .format("damageTakenMult", getDamageTakenIncrease(member, mods, exoticData))
                .format("cooldown", getSystemCooldown(member, mods, exoticData))
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
        stats.fluxDissipation.modifyMult(buffId, 1f + getDissipationIncrease(member, mods, exoticData) / 100f)
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        ActivatorManager.addActivator(ship, BigVentActivator(ship, member, mods, exoticData))
    }

    fun getSystemCooldown(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return COOLDOWN / getPositiveMult(member, mods, exoticData)
    }

    fun getDissipationIncrease(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return DISSIPATION_DECREASE * getNegativeMult(member, mods, exoticData)
    }

    fun getDamageTakenIncrease(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return DAMAGE_TAKEN_INCREASE * getNegativeMult(member, mods, exoticData)
    }

    inner class BigVentActivator(ship: ShipAPI, val member: FleetMemberAPI, val mods: ShipModifications, val exoticData: ExoticData): CombatActivator(ship) {
        private var lastActivation: Float = -1f

        override fun canActivate(): Boolean {
            return ship.currFlux >= getFluxVented() && !ship.engineController.isFlamedOut
        }

        fun getFluxVented(): Float {
            return (ship.maxFlux * FLUX_VENTED / 100f).coerceAtMost(FLUX_VENTED_MAX)
        }

        fun getTotalActiveDuration(): Float {
            return inDuration + activeDuration + outDuration
        }

        override fun getBaseInDuration(): Float {
            return 0.1f
        }

        override fun getBaseActiveDuration(): Float {
            return 0.6f
        }

        override fun getBaseOutDuration(): Float {
            return 0.2f
        }

        override fun hasCharges(): Boolean {
            return true
        }

        override fun getMaxCharges(): Int {
            return 2
        }

        override fun getBaseChargeRechargeDuration(): Float {
            return getSystemCooldown(member, mods, exoticData)
        }

        override fun getBaseCooldownDuration(): Float {
            return getTotalActiveDuration() * 2f
        }

        override fun shouldActivateAI(amount: Float): Boolean {
            val flags = ship.aiFlags
            val engageRange = aiData.engagementRange
            val immediateTarget: CombatEntityAPI? = aiData.immediateTarget

            var assignment: AssignmentInfo? =
                Global.getCombatEngine().getFleetManager(ship.owner).getTaskManager(ship.isAlly).getAssignmentFor(ship)
            val targetSpot: Vector2f? =
                    if (assignment != null && assignment.target != null && assignment.type != CombatAssignmentType.AVOID) {
                        assignment.target.location
                    } else {
                        null
                    }

            var desire = 0f
            if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                if (immediateTarget != null) {
                    desire += 1.25f
                } else {
                    desire += 0.75f
                }
            }
            if (flags.hasFlag(AIFlags.PURSUING)) {
                if (immediateTarget != null) {
                    desire += 0.75f
                } else if (targetSpot != null) {
                    desire += 0.5f
                } else {
                    desire += 0.25f
                }
            }
            if (flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
                if (immediateTarget != null) {
                    desire += 1f
                } else if (targetSpot != null) {
                    desire += 0.75f
                } else {
                    desire += 0.5f
                }
            }
            if (flags.hasFlag(AIFlags.TURN_QUICKLY)) {
                desire += 0.35f
            }
            if (flags.hasFlag(AIFlags.BACKING_OFF)) {
                if (immediateTarget != null) {
                    desire += 0.75f
                } else {
                    desire += 0.5f
                }
            }
            if (flags.hasFlag(AIFlags.DO_NOT_PURSUE)) {
                if (immediateTarget != null) {
                    desire -= 1f
                } else {
                    desire -= 0.5f
                }
            }
            if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                desire += 0.35f
            }
            if (flags.hasFlag(AIFlags.NEEDS_HELP) || flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
                desire += 1f
            }
            if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)) {
                if (immediateTarget != null) {
                    desire -= 0.5f
                } else {
                    desire += 0.75f
                }
            }

            var immediateTargetInRange = false
            if (immediateTarget != null && MathUtils.getDistance(immediateTarget, ship) < engageRange - ship.collisionRadius) {
                immediateTargetInRange = true
            }

            if (immediateTarget != null && !immediateTargetInRange) {
                desire += 0.5f
            }

            if (!immediateTargetInRange) {
                var desiredRange = 500f
                if (assignment != null
                    && (assignment.type == CombatAssignmentType.ENGAGE || assignment.type == CombatAssignmentType.HARASS || assignment.type == CombatAssignmentType.INTERCEPT || assignment.type == CombatAssignmentType.LIGHT_ESCORT || assignment.type == CombatAssignmentType.MEDIUM_ESCORT || assignment.type == CombatAssignmentType.HEAVY_ESCORT || assignment.type == CombatAssignmentType.STRIKE)
                ) {
                    desiredRange = engageRange
                }

                if (targetSpot != null && MathUtils.getDistance(targetSpot, ship.location) >= desiredRange) {
                    if (immediateTarget != null && MathUtils.getDistance(immediateTarget, targetSpot) <= engageRange) {
                        desire += 0.5f
                    } else if (immediateTarget != null) {
                        desire += 0.25f
                    } else {
                        desire += 0.75f
                    }
                }
            }

            if (assignment != null && assignment.type == CombatAssignmentType.RETREAT) {
                desire += 1.5f
            }

            if (ship.fluxLevel > getFluxVented() / ship.maxFlux) {
                desire += 0.5f + ((ship.fluxLevel / (getFluxVented() / ship.maxFlux)) - 1f)
            }

            val targetDesire: Float = if (charges <= 1) {
                    1f
                } else { // 2
                    0.5f
                }

            return desire >= targetDesire
        }

        override fun getDisplayText(): String {
            return StringUtils.getString("DriveFluxVent", "systemText")
        }

        override fun getStateText(): String {
            if (state.equals(State.READY)) {
                if (!canActivate()) {
                    return ""
                }
            }
            return super.getStateText()
        }

        /*override fun renderWorld(viewport: ViewportAPI?) {
            val spawnLoc = MathUtils.getRandomPointInCircle(ship.location, ship.collisionRadius * 0.66f)
            val particleVel = VectorUtils.getDirectionalVector(ship.location, spawnLoc)

            val localLoc = ship.getNearestPointOnBounds(spawnLoc)
            MagicRender.objectspace(Global.getSettings().getSprite("misc", "flux_smoke"),
                ship,
                localLoc,
                particleVel,
                Vector2f(32f, 32f),
                Vector2f(128f, 128f), 0f, 0f, true, ship.ventCoreColor, true, 0.1f, 0.4f, 0.25f, false)
        }*/

        override fun onActivate() {
            lastActivation = Global.getCombatEngine().getTotalElapsedTime(false)
            stats.hullDamageTakenMult.modifyPercent(buffId, getDamageTakenIncrease(member, mods, exoticData))
            stats.shieldDamageTakenMult.modifyPercent(buffId, getDamageTakenIncrease(member, mods, exoticData))
            stats.armorDamageTakenMult.modifyPercent(buffId, getDamageTakenIncrease(member, mods, exoticData))
        }

        override fun advance(amount: Float) {
            if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
                ship.engineController.fadeToOtherColor(this, engineColor, Color(0, 0, 0, 0), effectLevel, 0.67f)
                ship.engineController.extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel)

                stats.acceleration.modifyFlat(buffId, 5000f)
                stats.deceleration.modifyFlat(buffId, 5000f)
                stats.turnAcceleration.modifyFlat(buffId, 5000f)
                stats.maxTurnRate.modifyFlat(buffId, 15f)
                stats.maxTurnRate.modifyPercent(buffId, 100f)

                ship.fluxTracker.currFlux -= amount / getTotalActiveDuration() * getFluxVented()
            }

            if (state == State.ACTIVE) {
                var boostScale = 1f
                if (ship.engineController.isAcceleratingBackwards || ship.engineController.isDecelerating) {
                    boostScale *= 0.2f
                }
                stats.maxSpeed.modifyPercent(buffId, 150f * boostScale)

                val speed = ship.velocity.length()
                if (speed <= 0.1f) {
                    ship.velocity.set(VectorUtils.getDirectionalVector(ship.location, ship.velocity))
                }
                if (speed < 900f) {
                    if (ship.velocity.equals(Misc.ZERO)) {
                        ship.velocity.set(VectorUtils.rotate(Vector2f(1f, 0f), ship.facing))
                    }

                    ship.velocity.normalise()
                    ship.velocity.scale(speed + amount * 3600f * boostScale)
                }
            } else if (state == State.OUT) {
                val speed = ship.velocity.length()
                if (speed <= 0.1f) {
                    ship.velocity.set(VectorUtils.getDirectionalVector(ship.location, ship.velocity))
                }

                stats.maxSpeed.modifyFlat(buffId, (speed.coerceAtMost(200f) - amount * 3600f).coerceAtLeast(0f))

                if (speed > ship.mutableStats.maxSpeed.modifiedValue) {
                    if (ship.velocity.equals(Misc.ZERO)) {
                        ship.velocity.set(VectorUtils.rotate(Vector2f(1f, 0f), ship.facing))
                    }

                    ship.velocity.normalise()
                    ship.velocity.scale(speed - amount * 3600f)
                }
            }

            if (lastActivation >= 0f && shouldUnmodifyDamageTaken()) {
                lastActivation = -1f

                stats.hullDamageTakenMult.unmodify(buffId)
                stats.shieldDamageTakenMult.unmodify(buffId)
                stats.armorDamageTakenMult.unmodify(buffId)
            }
        }

        fun shouldUnmodifyDamageTaken(): Boolean {
            return Global.getCombatEngine().getTotalElapsedTime(false) - lastActivation >= 5f
        }

        override fun getHUDColor(): Color {
            var color = super.getHUDColor()
            if (lastActivation >= 0f && !shouldUnmodifyDamageTaken()) {
                val scalar = abs(sin(Global.getCombatEngine().getTotalElapsedTime(true) * 3))
                color = Color((255 - 100f * scalar) / 255f, (55f + 200f * scalar) / 255f, (200f - 200f * scalar) / 255f)
            }

            return color
        }

        override fun onStateSwitched(oldState: State) {
            if (state == State.COOLDOWN) {
                stats.maxSpeed.unmodify(buffId)
                stats.maxTurnRate.unmodify(buffId)
                stats.turnAcceleration.unmodify(buffId)
                stats.acceleration.unmodify(buffId)
                stats.deceleration.unmodify(buffId)
            }
        }
    }

    companion object {
        private const val ITEM = "et_drivevent"
        private const val DISSIPATION_DECREASE = -15f

        private val engineColor = Color(255, 50, 200, 255)

        private const val FLUX_VENTED = 20f
        private const val FLUX_VENTED_MAX = 4000f
        private const val COOLDOWN = 15f
        private const val DAMAGE_TAKEN_INCREASE = 20f
    }
}