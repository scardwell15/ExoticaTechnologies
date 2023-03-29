package exoticatechnologies.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.modifications.upgrades.UpgradesHandler

class ExoticaEveryFramePlugin :
    BaseEveryFrameCombatPlugin() {

    override fun advance(amount: Float, events: List<InputEventAPI>) {
        val ships: List<ShipAPI> = Global.getCombatEngine().ships;
        for (ship in ships) {
            val member = ship.fleetMember
            if (ship.fleetMember != null) {
                val mods = ShipModLoader.get(ship.fleetMember)
                if (mods != null) {
                    for (upgrade in UpgradesHandler.UPGRADES_LIST) {
                        val level = mods.getUpgrade(upgrade)
                        if (level <= 0) continue
                        upgrade.advanceInCombatAlways(ship, member, mods)
                    }

                    for (exotic in ExoticsHandler.EXOTIC_LIST) {
                        if (!mods.hasExotic(exotic)) continue
                        exotic.advanceInCombatAlways(ship, member, mods, mods.getExoticData(exotic)!!)
                    }
                }
            }
        }
    }
}