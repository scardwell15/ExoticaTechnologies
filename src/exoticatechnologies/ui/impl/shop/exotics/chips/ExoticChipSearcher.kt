package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipSearcher

class ExoticChipSearcher : ChipSearcher<ExoticSpecialItemPlugin>() {
    override fun filterChip(
        member: FleetMemberAPI,
        mods: ShipModifications,
        stack: CargoStackAPI,
        plugin: ExoticSpecialItemPlugin
    ): Boolean {
        return true
    }
}