package exoticatechnologies.modifications.exotics.types.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetDataAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import kotlin.math.pow

class GuerillaType : ExoticType("GUERILLA", colorOverlay = Color(140, 200, 125, 255), sprite = "graphics/icons/overlays/guerilla.png") {
    override fun getPositiveMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        val thresholdScale = 0.5f - getThresholdScale(member, 0.5f)
        return 1.75f - thresholdScale
    }

    override fun getNegativeMult(member: FleetMemberAPI, mods: ShipModifications): Float {
        val thresholdScale = getThresholdScale(member, 0.5f)
        return 1f + (0.5f - thresholdScale)
    }

    private fun getThreshold(): Float {
        return Global.getSettings().battleSize * 0.375f // 400 -> 150
    }

    private fun getThresholdScale(member: FleetMemberAPI, maxValue: Float): Float {
        return getThresholdBasedBonus(maxValue, getUnmodifiedFleetDP(member), getThreshold())
    }

    private fun getUnmodifiedFleetDP(member: FleetMemberAPI): Float {
        if (member.fleetData == null) return 0f

        if (member.fleetData.cacheClearedOnSync.containsKey(GUERILLA_CACHE_KEY))
            return member.fleetData.cacheClearedOnSync[GUERILLA_CACHE_KEY] as Float

        val dpOfFleet = getTotalUnmodifiedCombatDP(member.fleetData)
        member.fleetData.cacheClearedOnSync[GUERILLA_CACHE_KEY] = dpOfFleet
        return dpOfFleet
    }

    private fun getTotalUnmodifiedCombatDP(data: FleetDataAPI): Float {
        var dp = 0f
        for (curr in data.membersListCopy) {
            if (curr.isMothballed) continue
            if (BaseSkillEffectDescription.isCivilian(curr)) continue
            dp += getUnmodifiedDP(curr)
        }
        return dp
    }

    private fun getUnmodifiedDP(member: FleetMemberAPI): Float {
        return member.hullSpec.suppliesToRecover
    }

    fun getThresholdBasedBonus(maxBonus: Float, value: Float, threshold: Float): Float {
        val divisor = threshold + (value.coerceAtLeast(threshold) - threshold).pow(1.4f)
        val thresholdRatio = threshold / divisor
        return maxBonus * thresholdRatio
    }

    override fun getChanceMult(context: ShipModFactory.GenerationContext): Float {
        if (context.member.fleetData != null) {
            return getThresholdScale(context.member, 0.75f)
        }
        return 0.25f
    }

    override fun getDescriptionTranslation(member: FleetMemberAPI, mods: ShipModifications): StringUtils.Translation? {
        val condition = StringUtils.getTranslation("ExoticTypes", "GuerillaCondition")
            .format("thresholdDP", getThreshold())
            .format("fleetDP", getUnmodifiedFleetDP(member))
            .toStringNoFormats()

        return StringUtils.getTranslation("ExoticTypes", "TooltipTextCondition")
            .formatFloat("positiveMult", getPositiveMult(member, mods))
            .formatFloat("negativeMult", getNegativeMult(member, mods))
            .format("condition", condition, colorOverlay.setAlpha(255))
    }

    override fun getItemDescTranslation(): StringUtils.Translation? {
        return StringUtils.getTranslation("ExoticTypes", "GuerillaItemDescription")
    }

    companion object {
        val GUERILLA_CACHE_KEY = "EXOTICA_GUERILLA_DP_KEY"
    }
}