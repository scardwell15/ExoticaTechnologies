package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import org.magiclib.util.MagicUI
import java.awt.Color

class PhasedFighterTether(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color.GREEN
    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return Utilities.hasItem(fleet.cargo, ITEM)
    }

    override fun canApply(member: FleetMemberAPI, mods: ShipModifications?): Boolean {
        if (member.stats != null) {
            if (member.stats.numFighterBays.modifiedInt > 0) {
                return canApplyToVariant(member.variant)
            }
        }
        return false
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
                .format("fighterRefitTimeDecrease", getRefitTimeDecrease(member, mods, exoticData))
                .addToTooltip(tooltip, title)
        }
    }

    fun getRefitTimeDecrease(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData): Float {
        return (REFIT_TIME_DECREASE * getPositiveMult(member, mods, exoticData)).coerceAtMost(75f)
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        stats.fighterRefitTimeMult.modifyMult(buffId,  1 - getRefitTimeDecrease(member, mods, exoticData) / 100f)
    }

    override fun advanceInCombatAlways(
        ship: ShipAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        val replacements = getFreeReplacements(ship)
        val replacementInterval = getReplacementInterval(ship)
        if (replacementInterval != null) {
            MagicUI.drawInterfaceStatusBar(
                ship,
                replacementInterval.elapsed / replacementInterval.intervalDuration,
                RenderUtils.getAliveUIColor(),
                RenderUtils.getAliveUIColor(),
                0f,
                StringUtils.getString(key, "statusBarText"),
                replacements
            )
        }
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        var replacements = getFreeReplacements(ship)
        if (Global.getCombatEngine().isPaused) {
            return
        }

        if (replacements < calculateMaxReplacements(ship)) {
            advanceReplacementInterval(ship, amount)
        }

        if (replacements > 0) {
            for (bay in ship.launchBaysCopy) {
                for (wing in ship.allWings) {
                    for (fighter in wing.wingMembers) {
                        if (fighter.wing != null && fighter.hitpoints <= fighter.maxHitpoints * 0.33f) {
                            wing.orderReturn(fighter)

                            MagicRender.battlespace(
                                fighter.spriteAPI,
                                fighter.location, fighter.velocity,
                                Vector2f(fighter.spriteAPI.width, fighter.spriteAPI.height),
                                Vector2f(-fighter.spriteAPI.width * 0.25f, -fighter.spriteAPI.height * 0.25f),
                                fighter.facing + 90,
                                fighter.angularVelocity,
                                Color(200, 100, 255),
                                false,
                                0.1f,
                                1.5f,
                                0.2f
                            )

                            bay.land(fighter)

                            setFreeReplacements(ship, --replacements)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val REPLACEMENT_COUNT_ID = "et_fighterReplacements"
        private const val REPLACEMENT_INTERVAL_ID = "et_fighterReplacementInterval"
        private const val REFIT_TIME_DECREASE = 25f

        fun advanceReplacementInterval(ship: ShipAPI, amount: Float) {
            val replacementInterval = getReplacementInterval(ship)
            replacementInterval!!.advance(amount)
            if (replacementInterval.intervalElapsed()) {
                addFreeReplacements(ship, calculateAddedReplacements(ship))
            }
        }

        private fun getReplacementInterval(ship: ShipAPI): IntervalUtil? {
            if (!ship.customData.containsKey(REPLACEMENT_INTERVAL_ID)) {
                ship.setCustomData(REPLACEMENT_INTERVAL_ID, IntervalUtil(60f, 60f))
            }
            return ship.customData[REPLACEMENT_INTERVAL_ID] as IntervalUtil?
        }

        private fun getFreeReplacements(ship: ShipAPI): Int {
            if (!ship.customData.containsKey(REPLACEMENT_COUNT_ID)) {
                val maxReplacements = calculateMaxReplacements(ship)
                setFreeReplacements(ship, maxReplacements)
            }
            return ship.customData[REPLACEMENT_COUNT_ID] as Int
        }

        private fun setFreeReplacements(ship: ShipAPI, value: Int) {
            ship.setCustomData(REPLACEMENT_COUNT_ID, value)
        }

        private fun addFreeReplacements(ship: ShipAPI, value: Int) {
            setFreeReplacements(ship, getFreeReplacements(ship) + value)
        }

        private fun calculateAddedReplacements(ship: ShipAPI): Int {
            return Math.ceil((calculateMaxReplacements(ship) / 2f).toDouble()).toInt()
        }

        private fun calculateMaxReplacements(ship: ShipAPI): Int {
            if (ship.variant == null || ship.variant.wings == null) {
                return 1
            }
            var wingCount = 0
            for (wingId in ship.variant.wings) {
                val spec = Global.getSettings().getFighterWingSpec(wingId)
                wingCount += spec?.numFighters ?: 1
            }
            return wingCount
        }
    }
}