package exoticatechnologies.modifications.exotics.impl

import activators.ActivatorManager
import activators.drones.DroneActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.combat.DroneStrikeStats
import com.fs.starfarer.api.impl.combat.DroneStrikeStats.DroneMissileScript
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.combat.ExoticaCombatUtils
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getDistanceSq
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

class TerminatorSubsystems(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xC20AE0)

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
                .format("drones", TerminatorDroneActivator.maxDronesMap[member.hullSpec.hullSize])
                .addToTooltip(tooltip, title)
        }
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        ActivatorManager.addActivator(ship, TerminatorDroneActivator(ship))
    }

    override fun canUseExoticType(type: ExoticType): Boolean {
        return false
    }

    override fun shouldAffectModule(ship: ShipAPI?, module: ShipAPI?): Boolean {
        return false
    }

    class TerminatorDroneActivator(ship: ShipAPI) : DroneActivator(ship) {
        private val droneStrikeStats = DroneStrikeStats()
        private var weaponBackingField: WeaponAPI? = null
        private val weapon: WeaponAPI
            get() {
                if (weaponBackingField == null) {
                    weaponBackingField = Global.getCombatEngine().createFakeWeapon(ship, getWeaponId())
                }
                return weaponBackingField!!
            }

        companion object {
            val maxDronesMap: Map<HullSize, Int> = mapOf(
                HullSize.FRIGATE to 2,
                HullSize.DESTROYER to 3,
                HullSize.CRUISER to 4,
                HullSize.CAPITAL_SHIP to 5
            )
        }

        override fun hasCharges(): Boolean {
            return false
        }

        override fun getBaseActiveDuration(): Float {
            return 0f
        }

        override fun getBaseCooldownDuration(): Float {
            return 0.25f
        }

        override fun getDisplayText(): String {
            return "Terminator Drones"
        }

        override fun canActivate(): Boolean {
            return activeWings.isNotEmpty()
        }

        override fun hasSeparateDroneCharges(): Boolean {
            return true
        }

        override fun getMaxDroneCharges(): Int {
            return 2
        }

        override fun getDroneCreationTime(): Float {
            return 20f
        }

        override fun getMaxDeployedDrones(): Int {
            return maxDronesMap[ship.hullSize] ?: 1
        }

        override fun getDroneVariant(): String {
            return "terminator_single_wing"
        }

        fun getWeaponId(): String {
            return "terminator_missile"
        }

        override fun spawnDrone(): ShipAPI {
            val drone = super.spawnDrone()
            drone.explosionScale = 0.67f
            drone.explosionVelocityOverride = Vector2f()
            drone.explosionFlashColorOverride = Color(255, 100, 50, 255)
            return drone
        }

        override fun onActivate() {
            val target: ShipAPI? = findTarget(ship)
            if (target != null) {
                convertDrone(ship, target)
            }
        }

        fun convertDrone(ship: ShipAPI, target: ShipAPI) {
            val engine = Global.getCombatEngine()
            val drones: MutableList<ShipAPI> = mutableListOf()
            drones.addAll(activeWings.keys)

            drones.sortWith { o1, o2 ->
                val d1 = Misc.getDistance(o1.location, target.location)
                val d2 = Misc.getDistance(o2.location, target.location)
                sign(d1 - d2).toInt()
            }

            val drone = drones[0]
            val missile = engine.spawnProjectile(
                ship, weapon, getWeaponId(),
                Vector2f(drone.location), drone.facing, Vector2f(drone.velocity)
            ) as MissileAPI

            if (missile.ai is GuidedMissileAI) {
                val ai = missile.ai as GuidedMissileAI
                ai.target = target
            }
            //missile.setHitpoints(missile.getHitpoints() * drone.getHullLevel());
            missile.empResistance = 10000
            val base = missile.maxRange
            val max: Float = droneStrikeStats.getMaxRange(ship)
            missile.maxRange = max
            missile.maxFlightTime = missile.maxFlightTime * max / base

            activeWings.remove(drone)
            drone.wing.removeMember(drone)
            drone.wing = null
            drone.explosionFlashColorOverride = Color(255, 100, 50, 255)
            engine.addLayeredRenderingPlugin(DroneMissileScript(drone, missile))

//				engine.removeEntity(drone);
//				drone.getVelocity().set(0, 0);
//				drone.setHulk(true);
//				drone.setHitpoints(-1f);

            //float thickness = 16f;
            val thickness = 26f
            val coreWidthMult = 0.67f
            val arc = engine.spawnEmpArcVisual(
                ship.location, ship,
                missile.location, missile, thickness, Color(255, 100, 100, 255), Color.white
            )
            arc.coreWidthOverride = thickness * coreWidthMult
            arc.setSingleFlickerMode()
        }

        override fun getStateText(): String {
            if (state == State.READY) {
                if (findTarget(ship) == null) {
                    return StringUtils.getString("TerminatorSubsystems", "outOfRange")
                }
            }
            return super.getStateText()
        }

        fun findTarget(ship: ShipAPI): ShipAPI? {
            if (activeWings.isEmpty()) {
                return null
            }

            val range: Float = droneStrikeStats.getMaxRange(ship)
            val player = ship === Global.getCombatEngine().playerShip
            var target: ShipAPI? = null

            if (ship.shipTarget != null && ship.shipTarget.location.getDistanceSq(ship.location) <= range * range) {
                target = ship.shipTarget
            }

            // If not the player:
            // The AI sets forceNextTarget, so if we're here, that target got destroyed in the last frame
            // or it's using a different AI
            // so, find *something* as a failsafe
            if (!player) {
                val test = ship.aiFlags.getCustom(AIFlags.MANEUVER_TARGET)
                if (test is ShipAPI) {
                    target = test
                    val dist = Misc.getDistance(ship.location, target.location)
                    val radSum = ship.collisionRadius + target.collisionRadius
                    if (dist > range + radSum) target = null
                }
                if (target == null) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.mouseTarget, HullSize.FRIGATE, range, true)
                }
                return target
            }

            // Player ship
            if (target != null) return target // was set with R, so, respect that

            // otherwise, find the nearest thing to the mouse cursor, regardless of if it's in range
            target = Misc.findClosestShipEnemyOf(ship, ship.mouseTarget, HullSize.FIGHTER, Float.MAX_VALUE, true)
            if (target != null && target.isFighter) {
                val nearbyShip = Misc.findClosestShipEnemyOf(ship, target.location, HullSize.FRIGATE, 100f, false)
                if (nearbyShip != null) target = nearbyShip
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.location, HullSize.FIGHTER, range, true)
            }
            return target
        }

        private val goodAiFlags = setOf(
            AIFlags.PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL,
            AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT,
            AIFlags.IN_ATTACK_RUN
        )
        override fun shouldActivateAI(amount: Float): Boolean {
            var target = findTarget(ship)
            if (target != null) {
                var score = 0f
                score += (target.currFlux / target.maxFlux) * 10f

                if (target.fluxTracker.isOverloadedOrVenting) {
                    score += 8f
                }

                goodAiFlags
                    .filter { ship.aiFlags.hasFlag(it) }
                    .firstOrNull()
                    ?.let {
                        score += 8f
                    }

                var desiredScore = 10f
                if (charges >= 2) {
                    desiredScore = 5f
                } else if (charges >= 1) {
                    desiredScore = 7.5f
                }

                if (score > desiredScore) {
                    return true
                }
            }
            return false
        }
    }

    companion object {
        private const val RATE_OF_FIRE_BUFF = 100f
        private const val RATE_OF_FIRE_DEBUFF = -33f
        private const val COOLDOWN = 8
        private const val BUFF_DURATION = 5
        private const val DEBUFF_DURATION = 4
    }
}