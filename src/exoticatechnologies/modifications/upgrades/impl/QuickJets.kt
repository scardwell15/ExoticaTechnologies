package exoticatechnologies.modifications.upgrades.impl

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import kotlin.math.absoluteValue

class QuickJets(key: String, settings: JSONObject) : Upgrade(key, settings) {
    override var maxLevel: Int = 1

    override fun applyToShip(member: FleetMemberAPI, ship: ShipAPI, mods: ShipModifications) {
        MagicSubsystemsManager.addSubsystemToShip(ship, QuickTurnJets(ship))
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

    inner class QuickTurnJets(ship: ShipAPI) : MagicSubsystem(ship) {
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

        override fun advance(amount: Float, isPaused: Boolean) {
            if (isPaused) return

            if (state == State.OUT) {
                val speed = ship.angularVelocity

                stats.maxTurnRate.modifyFlat(
                    buffId,
                    (stats.maxTurnRate.getFlatStatMod(buffId).value - (15f / outDuration) * amount).coerceAtLeast(0f)
                )
                stats.maxTurnRate.modifyPercent(
                    buffId,
                    (stats.maxTurnRate.getPercentStatMod(buffId).value - (100f / outDuration) * amount).coerceAtLeast(0f)
                )

                if (speed.absoluteValue > ship.mutableStats.maxTurnRate.modifiedValue) {
                    val negative = speed < 0
                    if (negative) {
                        ship.angularVelocity =
                            (speed + amount * 4500f).coerceIn(-ship.mutableStats.maxTurnRate.modifiedValue..0f)
                    } else {
                        ship.angularVelocity =
                            (speed - amount * 4500f).coerceIn(0f..ship.mutableStats.maxTurnRate.modifiedValue)
                    }
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