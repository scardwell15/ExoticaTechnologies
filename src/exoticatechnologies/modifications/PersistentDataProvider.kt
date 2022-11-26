package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.campaign.listeners.CampaignEventListener

class PersistentDataProvider: ShipModLoader.Provider {
    fun getFromId(id: String): ShipModifications? {
        return shipModificationMap[id]
    }

    override fun get(member: FleetMemberAPI): ShipModifications? {
        return getFromId(member.id)
    }

    override fun set(member: FleetMemberAPI, mods: ShipModifications) {
        throw RuntimeException("This provider is not intended to be used.")

        shipModificationMap[member.id] = mods
    }

    override fun remove(member: FleetMemberAPI) {
        throw RuntimeException("This provider is not intended to be used.")

        shipModificationMap.remove(member.id)
    }

    companion object {
        @JvmStatic
        var inst: PersistentDataProvider = PersistentDataProvider()

        val ET_PERSISTENTUPGRADEMAP = "ET_MODMAP"
        private var loadedMap: MutableMap<String, ShipModifications>? = null

        @JvmStatic
        val shipModificationMap: MutableMap<String, ShipModifications>
            get() {
                if (loadedMap == null)
                    loadModificationData()
                return loadedMap!!
            }

        private fun loadModificationData() {
            if (Global.getSector().persistentData[ET_PERSISTENTUPGRADEMAP] == null) {
                Global.getSector().persistentData[ET_PERSISTENTUPGRADEMAP] = HashMap<String, ShipModifications>()
            }
            loadedMap = Global.getSector().persistentData[ET_PERSISTENTUPGRADEMAP] as MutableMap<String, ShipModifications>?
            CampaignEventListener.invalidateShipModifications()
        }
    }
}