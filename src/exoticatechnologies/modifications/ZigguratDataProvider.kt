package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

/**
 * The only ship in the game that changes variant upon recovery, to my knowledge.
 * VariantTagProvider will return updated versions of this if the player installs more mods.
 */
class ZigguratDataProvider: VariantTagProvider() {
    companion object {
        @JvmStatic
        var inst: ZigguratDataProvider = ZigguratDataProvider()
    }

    val ZIGGURAT_MODS_KEY = "ET_zigguratMods"

    fun setZigguratMods(member: FleetMemberAPI, mods: ShipModifications) {
        Global.getSector().persistentData[ZIGGURAT_MODS_KEY] = convertToJson(member, mods)
    }

    fun getZigguratMods(): ShipModifications? {
        return if (Global.getSector().persistentData.containsKey(ZIGGURAT_MODS_KEY))
            convertFromJson(Global.getSector().persistentData[ZIGGURAT_MODS_KEY] as String)
        else null
    }

    override fun get(member: FleetMemberAPI, variant: ShipVariantAPI): ShipModifications? {
        return if (member.hullId.contains("ziggurat")) getZigguratMods() else null
    }

    override fun set(member: FleetMemberAPI, variant: ShipVariantAPI, mods: ShipModifications) {
        if (member.hullId.contains("ziggurat")) {
            setZigguratMods(member, mods)
        }
    }

    override fun remove(member: FleetMemberAPI, variant: ShipVariantAPI) {
        //donothing
    }

    override fun setOnlyIfFirst(): Boolean {
        return false
    }
}