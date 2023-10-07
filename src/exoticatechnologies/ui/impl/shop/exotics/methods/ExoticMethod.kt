package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic

interface ExoticMethod {
    val key: String

    fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?
    ): String
    fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean
    fun canShow(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean
    fun getButtonText(exotic: Exotic): String
    fun getButtonTooltip(exotic: Exotic): String?
    fun getResourceMap(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?, hovered: Boolean): Map<String, Float>?

    fun canUseIfMarketIsNull(): Boolean {
        return false
    }

    fun shouldLoad(): Boolean {
        return true
    }
}