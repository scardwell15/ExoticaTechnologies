package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.exotics.ETExotics
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.upgrades.ETUpgrades
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.optFloat
import java.lang.ref.WeakReference
import java.util.WeakHashMap

open class VariantTagProvider : ShipModLoader.Provider {
    companion object {
        @JvmStatic
        var inst: VariantTagProvider = VariantTagProvider()
    }

    var currGets: Int = 0
    val maxGetsPerMember: Int = 10

    val cache: MutableMap<FleetMemberAPI, ShipModifications> = WeakHashMap()
    val EXOTICA_INDICATOR = "$\$EXOTICA$$"

    override fun get(member: FleetMemberAPI): ShipModifications? {
        val members: Int = Global.getSector()?.playerFleet?.numMembersFast ?: 0
        if (currGets++ >= maxGetsPerMember * members) {
            cache.clear()
            currGets = 0
        }

        val cacheMods: ShipModifications? = cache.keys
            .filter { it == member }
            .firstNotNullOfOrNull { cache[it] }

        if (cacheMods != null) {
            return cacheMods
        }

        val variant: ShipVariantAPI = member.variant
            ?: return null

        getFromVariant(variant)?.let {
            if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT || Global.getSector().campaignUI.currentCoreTab == CoreUITabId.FLEET) {
                return it
            } else {
                cache[member] = it
            }
            return it
        }
        return null
    }

    override fun set(member: FleetMemberAPI, mods: ShipModifications) {
        remove(member)
        member.variant.addTag(EXOTICA_INDICATOR + convertToJson(member, mods))
        cache[member] = mods
    }

    override fun remove(member: FleetMemberAPI) {
        removeFromTags(member.variant)
    }

    private fun removeFromTags(variant: ShipVariantAPI) {
        variant.tags.removeAll { it.startsWith(EXOTICA_INDICATOR) }
    }

    fun getFromVariant(variant: ShipVariantAPI): ShipModifications? {
        val exoticaTag: String? = variant.tags.firstOrNull { it.startsWith(EXOTICA_INDICATOR) }

        if (exoticaTag != null) {
            val jsonStr = exoticaTag.replace(EXOTICA_INDICATOR, "")
            return convertFromJson(jsonStr)
        }

        return null
    }

    fun convertFromJson(json: String): ShipModifications {
        try {
            val modsObj = JSONObject(json)
            return ShipModifications(modsObj)
        } catch (ex: JSONException) {
            throw RuntimeException("Failed to load Exotica modifications object from variant tags.", ex)
        }
    }

    fun convertToJson(member:FleetMemberAPI, mods: ShipModifications): String {
        try {
            return mods.toJson(member).toString()
        } catch (ex: JSONException) {
            throw RuntimeException("Failed to save Exotica object.")
        }
    }
}