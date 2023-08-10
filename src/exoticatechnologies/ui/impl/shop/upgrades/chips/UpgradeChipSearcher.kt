package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipSearcher

class UpgradeChipSearcher : ChipSearcher<UpgradeSpecialItemPlugin>() {
    override fun filterChip(
        member: FleetMemberAPI,
        mods: ShipModifications,
        stack: CargoStackAPI,
        plugin: UpgradeSpecialItemPlugin
    ): Boolean {
        val upgrade = plugin.upgrade!!
        return plugin.upgradeLevel > mods.getUpgrade(upgrade)
    }
}