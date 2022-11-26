package exoticatechnologies.ui.impl.shop

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications

interface ModsModifier {
    var listeners: MutableList<ModChangeListener>

    fun modifiedMods(member: FleetMemberAPI, mods: ShipModifications) {
        listeners.forEach {
            it.changedMods(member, mods)
        }
    }

    fun addModChangeListener(listener: ModChangeListener) {
        listeners.add(listener)
    }

    fun interface ModChangeListener {
        fun changedMods(member: FleetMemberAPI, mods: ShipModifications)
    }
}