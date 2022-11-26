package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.campaign.listeners.CampaignEventListener

class PersistentDataProvider: VariantTagProvider() {
    fun getFromId(id: String): ShipModifications? {
        return shipModificationMap[id]
    }

    override fun get(member: FleetMemberAPI): ShipModifications? {
        val mods: ShipModifications? = getFromId(member.id)
        if (mods != null) {
            super.set(member, mods) //set variant tag
        }
        return mods
    }

    override fun set(member: FleetMemberAPI, mods: ShipModifications) {
        throw RuntimeException("This provider is not intended to be used.")

        shipModificationMap[member.id] = mods
    }

    override fun remove(member: FleetMemberAPI) {
        shipModificationMap.remove(member.id)
    }

    companion object {
        @JvmStatic
        var inst: PersistentDataProvider = PersistentDataProvider()

        val ET_PERSISTENTUPGRADEMAP = "ET_MODMAP"

        @JvmStatic
        val shipModificationMap: MutableMap<String, ShipModifications>
            get() {
                if (Global.getSector().persistentData[ET_PERSISTENTUPGRADEMAP] == null) {
                    Global.getSector().persistentData[ET_PERSISTENTUPGRADEMAP] = HashMap<String, ShipModifications>()
                }
                return Global.getSector().persistentData[ET_PERSISTENTUPGRADEMAP] as MutableMap<String, ShipModifications>
            }
    }
}