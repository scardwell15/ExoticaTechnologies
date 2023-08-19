package exoticatechnologies.modifications.conditions

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.operators.Operator
import exoticatechnologies.refit.checkRefitVariant
import exoticatechnologies.util.Utilities
import org.apache.log4j.Logger
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.optFloat

abstract class OperatorCondition : Condition {
    companion object {
        @JvmStatic
        private val log: Logger = Logger.getLogger(OperatorCondition::class.java)
    }

    private var operatorString: String = "none"
    protected val operator: Operator
        get() = OperatorDict.get(operatorString)
    protected var expected: Any = ""
    protected var extra: Any? = ""
    private var chipSkips = false
    override var cannotApplyReason: String? = null
    override var weight: Float = 0f
    override var weightOnly: Boolean = false
    private var maxWeight: Float = -1f
    private var weightScales: Boolean = false

    override fun setup(condition: JSONObject) {
        operatorString = condition.getString("operator")

        expected = try {
            condition.getJSONArray("expected").toList()
        } catch (e: JSONException) {
            condition.get("expected")
        }

        extra = condition.opt("extra")
        weight = condition.optFloat("weight", 0f)
        weightScales = condition.optBoolean("weightScales")
        weightOnly = condition.optBoolean("weightOnly")
        maxWeight = condition.optFloat("maxWeight", -1f)
        chipSkips = condition.optBoolean("chipSkips")
        cannotApplyReason = condition.optString("cannotApplyReason", null)
    }

    override fun compare(member: FleetMemberAPI, mods: ShipModifications?, chipKey: String?): Boolean {
        if (weightOnly) return true

        if (chipSkips && chipKey != null) {
            if (member.fleetData.fleet.isPlayerFleet) {
                if (Utilities.hasChip(member.fleetData.fleet.cargo, chipKey)) {
                    return true
                }
            }
        }

        val actual = getActual(member, mods) ?: return false

        try {
            return operator.matches(actual, expected, extra)
        } catch (ex: Exception) {
            log.error("$key threw exception while checking condition with arguments exp=[$expected], ext=[$extra]")
            throw ex
        }
    }

    abstract fun getActual(member: FleetMemberAPI, mods: ShipModifications?, variant: ShipVariantAPI = member.checkRefitVariant()): Any?

    override fun calculateWeight(member: FleetMemberAPI, mods: ShipModifications?): Float {
        var calculatedWeight = weight
        if (weightScales) {
            val actual = getActual(member, mods)

            if (actual != null) {
                calculatedWeight *= operator.calculateWeight(actual, expected, extra)
            }
        }

        if (maxWeight >= 0f) {
            calculatedWeight.coerceAtMost(maxWeight)
        }

        return calculatedWeight
    }
}