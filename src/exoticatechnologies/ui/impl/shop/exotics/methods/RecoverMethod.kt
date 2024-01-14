package exoticatechnologies.ui.impl.shop.exotics.methods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.Utilities

class RecoverMethod : DestroyMethod() {
    override val key: String = "recover"

    override fun apply(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?
    ): String {
        val fleet: CampaignFleetAPI = Global.getSector().playerFleet
        
        val exoticData = mods.getExoticData(exotic)
        val exoticType = exoticData?.type ?: ExoticType.NORMAL

        val stack = Utilities.getExoticChip(fleet.cargo, exotic.key, exoticType.nameKey)
        if (stack != null) {
            stack.add(1f)
        } else {
            fleet.cargo.addSpecial(exotic.getNewSpecialItemData(exoticType), 1f)
        }

        Global.getSector().playerStats.storyPoints--

        /**
         * Super call
         */
        super.apply(member, variant, mods, exotic, market)

        return StringUtils.getString("ExoticsDialog", "ExoticRecovered")
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, exotic: Exotic, market: MarketAPI?): Boolean {
        if (Global.getSector().playerStats.storyPoints >= 1f) {
            return super.canUse(member, mods, exotic, market)
        }
        return false
    }

    override fun getButtonText(exotic: Exotic): String {
        return StringUtils.getString("ExoticsDialog", "RecoverExotic")
    }

    override fun getResourceMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        exotic: Exotic,
        market: MarketAPI?,
        hovered: Boolean
    ): Map<String, Float>? {
        if (hovered) {
            val resourceCosts: MutableMap<String, Float> = mutableMapOf()
            resourceCosts[Utilities.STORY_POINTS] = 1f

            val stack = Utilities.getExoticChip(Global.getSector().playerFleet.cargo, exotic.key)
            if (stack != null) {
                resourceCosts.put(Utilities.formatSpecialItem(stack.specialDataIfSpecial), -1f)
            } else {
                resourceCosts.put(
                    "&" + StringUtils.getTranslation("ShipListDialog", "ChipName")
                        .format("name", exotic.name)
                        .toStringNoFormats(), -1f)
            }

            if (exotic.getExtraBandwidth(member, mods, mods.getExoticData(exotic)) > 0) {
                resourceCosts[Bandwidth.BANDWIDTH_RESOURCE] = -exotic.getExtraBandwidth(
                    member,
                    mods,
                    mods.getExoticData(exotic)
                )
            }

            return resourceCosts
        }
        return null
    }
}