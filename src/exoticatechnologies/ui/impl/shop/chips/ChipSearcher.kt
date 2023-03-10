package exoticatechnologies.ui.impl.shop.chips

import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.cargo.CrateItemPlugin
import exoticatechnologies.modifications.ModSpecialItemPlugin
import exoticatechnologies.modifications.Modification
import exoticatechnologies.modifications.ShipModifications

open class ChipSearcher<T : ModSpecialItemPlugin> {
    /**
     * gets all valid upgrade chips for member from cargo
     */
    fun getChips(cargo: CargoAPI, member: FleetMemberAPI, mods: ShipModifications, mod: Modification): List<CargoStackAPI> {
        val stacks: List<CargoStackAPI> = cargo.stacksCopy
            .flatMap { stack ->
                if (stack.plugin is CrateItemPlugin)
                    getChipsFromCrate(member, stack.plugin as CrateItemPlugin, mods, mod)
                else
                    listOf(stack)
            }
            .filter { it.plugin is ModSpecialItemPlugin }
            .map { it to it.plugin as ModSpecialItemPlugin }
            .filter { (_, plugin) -> plugin.modId == mod.key }
            .filter { (stack, plugin) -> filterModChip(member, mods, stack, plugin) }
            .map { (stack, _) -> stack }

        return stacks
    }
    
    private fun filterModChip(member: FleetMemberAPI, mods: ShipModifications, stack: CargoStackAPI, plugin: ModSpecialItemPlugin): Boolean {
        try {
            return filterChip(member, mods, stack, plugin as T)
        } catch (e: ClassCastException) {
            return false
        }
    }

    open fun filterChip(member: FleetMemberAPI, mods: ShipModifications, stack: CargoStackAPI, plugin: T): Boolean {
        return true
    }

    /**
     * gets all valid upgrade chips for member from crate
     */
    fun getChipsFromCrate(member: FleetMemberAPI, plugin: CrateItemPlugin, mods: ShipModifications, mod: Modification): List<CargoStackAPI> {
        return getChips(plugin.cargo, member, mods, mod)
    }
}