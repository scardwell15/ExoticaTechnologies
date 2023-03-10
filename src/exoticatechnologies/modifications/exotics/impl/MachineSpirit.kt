package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.StringUtils
import org.json.JSONObject

class MachineSpirit(key: String, settingsObj: JSONObject) : Exotic(key, settingsObj) {
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
                .format("bandwidthIncrease", BANDWIDTH_INCREASE * getPositiveMult(member, mods, exoticData))
                .addToTooltip(tooltip, title)
        }
    }

    override fun getResourceCostMap(
        fm: FleetMemberAPI,
        mods: ShipModifications,
        market: MarketAPI
    ): MutableMap<String, Float> {
        return HashMap()
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        fm: FleetMemberAPI,
        mods: ShipModifications,
        data: ExoticData
    ) {
        onInstall(fm)
    }

    /**
     * extra bandwidth added directly to ship.
     *
     * @param member
     * @param mods
     * @param exoticData
     * @return
     */
    override fun getExtraBandwidth(member: FleetMemberAPI, mods: ShipModifications, exoticData: ExoticData?): Float {
        return BANDWIDTH_INCREASE * getPositiveMult(member, mods, exoticData)
    }

    companion object {
        private const val BANDWIDTH_INCREASE = 60
    }
}