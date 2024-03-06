package exoticatechnologies.ui2.util.chip

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin

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