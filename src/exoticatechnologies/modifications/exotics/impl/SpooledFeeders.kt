package exoticatechnologies.modifications.exotics.impl

import activators.ActivatorManager
import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.combat.ExoticaCombatUtils
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import java.awt.Color
import kotlin.math.abs

class SpooledFeeders(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(0xD93636)
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
                .format("firerateBoost", RATE_OF_FIRE_BUFF)
                .formatFloat("boostTime", BUFF_DURATION * getPositiveMult(member, mods, exoticData))
                .format("firerateMalus", abs(RATE_OF_FIRE_DEBUFF))
                .formatFloat("malusTime", DEBUFF_DURATION * getNegativeMult(member, mods, exoticData))
                .formatFloat("cooldownTime", COOLDOWN)
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
        ActivatorManager.addActivator(ship, SpooledActivator(member, mods, exoticData))
    }

    inner class SpooledActivator(val member: FleetMemberAPI, val mods: ShipModifications, val exoticData: ExoticData) :
        CombatActivator() {
        override fun getDisplayText(): String {
            return Global.getSettings().getString(this@SpooledFeeders.key, "systemText")
        }

        override fun getActiveDuration(): Float {
            return BUFF_DURATION * getPositiveMult(member, mods, exoticData)
        }

        override fun getOutDuration(): Float {
            return DEBUFF_DURATION * getNegativeMult(member, mods, exoticData)
        }

        override fun getCooldownDuration(): Float {
            return COOLDOWN.toFloat()
        }

        override fun onStateSwitched(ship: ShipAPI, state: State) {
            if (state == State.ACTIVE) {
                ship.mutableStats.ballisticRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_BUFF / 100f)
                ship.mutableStats.energyRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_BUFF / 100f)
                for (buffWeapon in ship.allWeapons) {
                    if (buffWeapon.cooldownRemaining > buffWeapon.cooldown / 2f) {
                        buffWeapon.setRemainingCooldownTo(buffWeapon.cooldown / 2f)
                    }
                }

                ship.addAfterimage(
                    Color(255, 0, 0, 150),
                    0f,
                    0f,
                    0f,
                    0f,
                    6f,
                    0f,
                    this.activeDuration,
                    0.25f,
                    true,
                    false,
                    true
                )
            }

            if (state == State.OUT) {
                ship.mutableStats.ballisticRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_DEBUFF / 100f)
                ship.mutableStats.energyRoFMult.modifyMult(buffId, 1 + RATE_OF_FIRE_DEBUFF / 100f)

                ship.addAfterimage(
                    Color(0, 0, 255, 150),
                    0f,
                    0f,
                    0f,
                    0f,
                    3f,
                    0.25f,
                    this.outDuration,
                    0.1f,
                    true,
                    false,
                    true
                )
            }

            if (state == State.COOLDOWN) {
                ship.mutableStats.ballisticRoFMult.unmodifyMult(buffId)
                ship.mutableStats.energyRoFMult.unmodifyMult(buffId)
            }
        }

        override fun shouldActivateAI(ship: ShipAPI): Boolean {
            val target = ship.shipTarget
            if (target != null) {
                var score = 0f
                score += (target.currFlux / target.maxFlux) * 10f

                if (target.fluxTracker.isOverloadedOrVenting) {
                    score += 8f
                }

                var dist = Misc.getDistance(ship.location, target.location)
                if (dist > ExoticaCombatUtils.getMaxWeaponRange(ship, false)) {
                    return false
                }

                var avgRange = ExoticaCombatUtils.getAverageWeaponRange(ship, false)
                score += (avgRange / dist).coerceAtMost(8f)

                if (score > 10f) {
                    return true
                }
            }
            return false
        }
    }

    companion object {
        private const val ITEM = "et_ammospool"
        private const val RATE_OF_FIRE_BUFF = 100f
        private const val RATE_OF_FIRE_DEBUFF = -33f
        private const val COOLDOWN = 12
        private const val BUFF_DURATION = 5
        private const val DEBUFF_DURATION = 4
    }
}