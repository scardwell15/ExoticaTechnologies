package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.util.StringUtils

open class DestroyMethod : ExoticMethod {
    override val key: String = "destroy"

    override fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?
    ): String {
        mods.removeExotic(exotic)
        exotic.onDestroy(member)

        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)

        return StringUtils.getString("ExoticsDialog", "ExoticDestroyed")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        if (mods.hasExotic(exotic)) {
            var shipBandwidth: Float = mods.getBandwidthWithExotics(member)
            var extraBandwidth: Float = 0f
            if (exotic.getExtraBandwidth(member, mods, mods.getExoticData(exotic)) > 0f) {
                extraBandwidth = exotic.getExtraBandwidth(member, mods, mods.getExoticData(exotic))
            } else if (exotic.getExtraBandwidthPurchaseable(member, mods, mods.getExoticData(exotic)) > 0f) {
                extraBandwidth = exotic.getExtraBandwidthPurchaseable(member, mods, mods.getExoticData(exotic))
            }

            if (extraBandwidth > 0) {
                return (shipBandwidth - mods.getUsedBandwidth()) > extraBandwidth
            }
            return true
        }
        return false
    }

    override fun canShow(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        return true
    }

    override fun getButtonText(exotic: Exotic): String {
        return StringUtils.getString("ExoticsDialog", "DestroyExotic")
    }

    override fun getButtonTooltip(exotic: Exotic): String? {
        return null
    }

    override fun getResourceMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?,
        hovered: Boolean
    ): Map<String, Float>? {
        if (hovered) {
            if (exotic.getExtraBandwidth(member, mods, mods.getExoticData(exotic)) > 0) {
                val resourceCosts: MutableMap<String, Float> = mutableMapOf()
                resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = -exotic.getExtraBandwidth(
                    member,
                    mods,
                    mods.getExoticData(exotic)
                )
                return resourceCosts
            }
        }
        return null
    }
}