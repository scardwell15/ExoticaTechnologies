package exoticatechnologies.modifications

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.conditions.Condition
import exoticatechnologies.modifications.conditions.ConditionDict
import org.apache.log4j.Logger
import org.json.JSONObject
import java.awt.Color

abstract class Modification(val key: String, val settings: JSONObject) {
    companion object {
        private val log = Logger.getLogger(Modification::class.java)
    }

    var name: String = settings.getString("name")
    var tag: String? = settings.optString("tag")
    open var color: Color = Color.white
    protected abstract val icon: String
    val conditions: MutableList<Condition> = mutableListOf()

    init {
        if (settings.has("conditions")) {
            conditions.addAll(ConditionDict.getCondsFromJSONArray(settings.getJSONArray("conditions")))
        }
    }

    /**
     * Checks tag against all modifications in mods.
     * @return true if null tag, null mods, or no conflicting mod was found. false if a conflicting mod was found.
     */
    open fun checkTags(member: FleetMemberAPI?, mods: ShipModifications?, tag: String?): Boolean {
        if (tag.isNullOrEmpty()) {
            return true
        }

        return mods?.getModsThatConflict(tag)?.none { it != this } ?: true
    }

    open fun checkConditions(member: FleetMemberAPI?, mods: ShipModifications?): Boolean {
        try {
            return conditions
                .none { !it.compare(member!!, mods, key) }
        } catch (ex: Exception) {
            log.error("$name threw exception while checking conditions", ex)
            throw ex
        }

    }

    open fun getCannotApplyReasons(member: FleetMemberAPI, mods: ShipModifications?): List<String> {
        return conditions
            .filter { !it.compare(member, mods, key) }
            .mapNotNull { it.cannotApplyReason }
    }

    open fun shouldLoad(): Boolean {
        return true
    }

    open fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI): Boolean {
        return true
    }

    open fun canApply(member: FleetMemberAPI, mods: ShipModifications?): Boolean {
        return checkConditions(member, mods) && checkTags(member, mods, tag) && canApplyToVariant(member.variant)
    }

    open fun canApplyToVariant(variant: ShipVariantAPI): Boolean {
        return true
    }
}