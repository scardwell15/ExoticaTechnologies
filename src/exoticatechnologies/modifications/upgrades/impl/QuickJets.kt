package exoticatechnologies.modifications.upgrades.impl

import activators.ActivatorManager
import activators.CombatActivator
import activators.drones.DroneActivator
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.math.absoluteValue

class QuickJets(key: String, settings: JSONObject) : Upgrade(key, settings) {
    override var maxLevel: Int = 1

    override fun applyToShip(member: FleetMemberAPI, ship: ShipAPI, mods: ShipModifications) {
        ActivatorManager.addActivator(ship, QuickTurnJets(ship))
    }

    override fun shouldAffectModule(ship: ShipAPI?, module: ShipAPI?): Boolean {
        return false
    }

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        expand: Boolean
    ): TooltipMakerAPI {
        val imageText = tooltip.beginImageWithText(iconPath, 64f)
        imageText.addPara("$name (%s)", 0f, color, mods.getUpgrade(this).toString())
        if (expand) {
            StringUtils.getTranslation("QuickJets", "tooltip")
                .addToTooltip(imageText)
        }
        tooltip.addImageWithText(5f)

        return imageText
    }

    override fun showStatsInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        StringUtils.getTranslation("QuickJets", "tooltip")
            .addToTooltip(tooltip)
    }

    inner class QuickTurnJets(ship: ShipAPI) : CombatActivator(ship) {
        override fun getBaseActiveDuration(): Float {
            return 1.5f
        }

        override fun getBaseCooldownDuration(): Float {
            return 10f
        }

        override fun getOutDuration(): Float {
            return 0.6f
        }

        override fun shouldActivateAI(amount: Float): Boolean {
            ship.shipTarget?.let { target ->
                val targetDir = VectorUtils.getAngle(ship.location, target.location)

                if (MathUtils.getShortestRotation(ship.facing, targetDir).absoluteValue > 30) {
                    return true
                }
            }
            return false
        }

        override fun onStateSwitched(oldState: State?) {
            if (state == State.IN || state == State.ACTIVE) {
                stats.turnAcceleration.modifyPercent(buffId, 350f)
                stats.maxTurnRate.modifyFlat(buffId, 15f)
                stats.maxTurnRate.modifyPercent(buffId, 100f)
            }
        }

        override fun onFinished() {
            stats.turnAcceleration.unmodify(buffId)
            stats.maxTurnRate.unmodify(buffId)
        }

        override fun advance(amount: Float) {
            if (state == State.OUT) {
                var speed = ship.angularVelocity
                if (speed <= 0.1f) {
                    speed = 0.1f
                }

                stats.maxTurnRate.modifyFlat(buffId, (speed.coerceAtMost(200f) - amount * 4500f).coerceAtLeast(0f))
                if (speed > ship.mutableStats.maxTurnRate.modifiedValue) {
                    ship.angularVelocity = speed - amount * 4500f
                }
            }
        }

        override fun getDisplayText(): String {
            return this@QuickJets.name
        }
    }

    companion object {
        const val buffId = "ExoticaQuickJets"
    }
}