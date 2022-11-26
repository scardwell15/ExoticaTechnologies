package exoticatechnologies.modifications

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial

class ShipModLoader {
    private var providers: List<Provider> = mutableListOf(
        VariantTagProvider.inst,
        ZigguratDataProvider.inst,
        PersistentDataProvider.inst
    )

    private fun getData(member: FleetMemberAPI): ShipModifications? {
        return providers.firstNotNullOfOrNull { it.get(member) }
    }

    private fun saveData(member: FleetMemberAPI, mods: ShipModifications) {
        for (i in providers.indices) {
            val provider = providers[i]
            if (!provider.setOnlyIfFirst() || i == 0) {
                provider.set(member, mods)
            }
        }
    }

    private fun removeData(member: FleetMemberAPI) {
        providers.forEach { it.remove(member) }
    }

    companion object {
        private val inst = ShipModLoader()

        @JvmStatic
        fun get(member: FleetMemberAPI): ShipModifications? {
            return inst.getData(member)
        }

        @JvmStatic
        fun set(member: FleetMemberAPI, mods: ShipModifications) {
            return inst.saveData(member, mods)
        }

        @JvmStatic
        fun remove(member: FleetMemberAPI) {
            return inst.removeData(member)
        }

        @JvmStatic
        fun getForSpecialData(shipData: ShipRecoverySpecial.PerShipData): ShipModifications? {
            if (shipData.getVariant() != null) {
                val mods = VariantTagProvider.inst.getFromVariant(shipData.getVariant())
                if (mods != null) {
                    return mods
                }
            }

            if (shipData.fleetMemberId != null) {
                val mods = PersistentDataProvider.inst.getFromId(shipData.fleetMemberId)
                if (mods != null) {
                    return mods
                }
            }

            return null
        }
    }

    interface Provider {
        fun get(member: FleetMemberAPI): ShipModifications?
        fun set(member: FleetMemberAPI, mods: ShipModifications)
        fun remove(member: FleetMemberAPI)

        fun setOnlyIfFirst(): Boolean {
            return true
        }
    }
}