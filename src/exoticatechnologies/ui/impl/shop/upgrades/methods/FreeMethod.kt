package exoticatechnologies.ui.impl.shop.upgrades.methods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.ETModPlugin
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import lombok.Getter

class FreeMethod : DefaultUpgradeMethod() {
    @Getter
    override var key = "free"
    override fun getOptionText(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): String {
        return "Free"
    }

    override fun getOptionTooltip(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): String {
        return "For free."
    }

    override fun canUse(member: FleetMemberAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): Boolean {
        return ETModPlugin.isDebugUpgradeCosts()
    }

    override fun canShow(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?
    ): Boolean {
        return canUse(member, mods, upgrade, market)
    }

    override fun apply(member: FleetMemberAPI, variant: ShipVariantAPI, mods: ShipModifications, upgrade: Upgrade, market: MarketAPI?): String {
        mods.putUpgrade(upgrade)
        ShipModLoader.set(member, variant, mods)
        ExoticaTechHM.addToFleetMember(member, variant)

        return StringUtils.getTranslation("UpgradesDialog", "UpgradePerformedSuccessfully")
            .format("name", upgrade.name)
            .format("level", mods.getUpgrade(upgrade))
            .toString()
    }

    override fun getResourceCostMap(
        member: FleetMemberAPI,
        mods: ShipModifications,
        upgrade: Upgrade,
        market: MarketAPI?,
        hovered: Boolean
    ): Map<String, Float> {
        return HashMap()
    }
}