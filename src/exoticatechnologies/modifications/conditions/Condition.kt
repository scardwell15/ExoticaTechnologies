package exoticatechnologies.modifications.conditions

import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.impl.*
import exoticatechnologies.modifications.exotics.ETExotics
import exoticatechnologies.modifications.upgrades.ETUpgrades
import exoticatechnologies.util.Utilities
import org.apache.log4j.Logger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface Condition {
    val key: String
    var cannotApplyReason: String?

    fun setup(condition: JSONObject)
    fun compare(member: FleetMemberAPI, mods: ShipModifications?, chipKey: String?): Boolean
}

abstract class OperatorCondition: Condition {
    val log: Logger = Logger.getLogger(OperatorCondition::class.java)
    private var operatorString: String = "none"
    protected val operator: Operator
        get() = Operator.get(operatorString)
    protected var expected: Any = ""
    protected var extra: Any? = ""
    private var chipSkips = false
    override var cannotApplyReason: String? = null

    override fun setup(condition: JSONObject) {
        operatorString = condition.getString("operator")

        expected = try {
            condition.getJSONArray("expected").toList()
        } catch (e: JSONException) {
            condition.get("expected")
        }

        extra = condition.opt("extra")
        chipSkips = condition.optBoolean("chipSkips")
        cannotApplyReason = condition.optString("cannotApplyReason", null)
    }

    override fun compare(member: FleetMemberAPI, mods: ShipModifications?, chipKey: String?): Boolean {
        if (chipSkips && chipKey != null) {
            if (member.fleetData.fleet.isPlayerFleet) {
                if (Utilities.hasChip(member.fleetData.fleet.cargo, chipKey)) {
                    return true
                }
            }
        }

        val actual = getActual(member, mods) ?: return false

        try {
            return operator.method.invoke(actual, expected, extra)
        } catch (ex: Exception) {
            log.error("$key threw exception while checking condition with arguments exp=[$expected], ext=[$extra]")
            throw ex
        }
    }

    abstract fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any?
}

enum class Operator(val string: String, val method: (Any, Any, Any?) -> Boolean) {
    ge(">=", { actual, expected, extra -> (actual as Number).toDouble() >= (expected as Number).toDouble() }),
    g(">", { actual, expected, extra -> (actual as Number).toDouble() > (expected as Number).toDouble() }),
    e("==", { actual, expected, extra -> actual == expected }),
    l("<", { actual, expected, extra -> (expected as Number).toDouble() > actual as Double }),
    le("<=", { actual, expected, extra -> (actual as Number).toDouble() <= (expected as Number).toDouble() }),
    ne("!=", { actual, expected, extra -> actual != expected }),
    In("in", { actual, expected, extra -> inMethod(actual, expected, extra) }),
    notIn("notIn", { actual, expected, extra -> notInMethod(actual, expected, extra) });

    companion object {
        fun get(operatorString: String): Operator {
            try {
                return Operator.values().first { it.string == operatorString }
            } catch (e: NoSuchElementException) {
                throw Exception("$operatorString is not a valid operator. Use ${combineStrings()} as valid operators.")
            }
        }

        fun combineStrings(): String {
            return Operator.values()
                .map { it.string }
                .joinToString(", ")
        }
    }
}

fun inMethod(actual: Any, expected: Any, extra: Any?): Boolean {
    if (actual is ETUpgrades) {
        if (expected is String) {
            return actual.getUpgrade(expected) > 0
        } else if (expected is List<*>) {
            return expected
                .any { actual.getUpgrade(it as String) > 0 }
        }
    } else if (actual is ETExotics) {
        if (expected is String) {
            return actual.hasExotic(expected)
        } else if (expected is List<*>) {
            return expected
                .any { actual.hasExotic(it as String) }
        }
    } else if (actual is List<*>) {
        if (expected is String) {
            return actual.contains(expected)
        } else if (expected is List<*>) {
            return expected.any { actual.contains(it) }
        }
    } else {
        return (expected as List<*>).contains(actual.toString())
    }
    throw IllegalArgumentException("in operator only takes String or JSON array")
}

fun notInMethod(actual: Any, expected: Any, extra: Any?): Boolean {
    if (actual is ETUpgrades) {
        if (expected is String) {
            return actual.getUpgrade(expected) == 0
        } else if (expected is List<*>) {
            return expected
                .none { actual.getUpgrade(it as String) > 0 }
        }
    } else if (actual is ETExotics) {
        if (expected is String) {
            return !actual.hasExotic(expected)
        } else if (expected is List<*>) {
            return expected
                .none { actual.hasExotic(it as String) }
        }
    } else if (actual is List<*>) {
        if (expected is String) {
            return !actual.contains(expected)
        } else if (expected is List<*>) {
            return expected.none { actual.contains(it) }
        }
    } else {
        return !(expected as List<*>).contains(actual.toString())
    }
    throw IllegalArgumentException("notIn operator only takes String or JSON array")
}

fun JSONArray.toList(): List<String> {
    val list: MutableList<String> = mutableListOf()
    for (i in 0 until this.length()) {
        list.add(this.get(i).toString())
    }
    return list
}

abstract class ConditionDict {
    companion object {
        private val log: Logger = Logger.getLogger(ConditionDict::class.java)
        private var mutableDict: MutableMap<String, Condition>? = null
        private val dict: Map<String, Condition>
            get() {
                if (mutableDict == null) {
                    mutableDict = mutableMapOf()

                    listOf(
                        LaunchBays(),
                        Faction(),
                        Exotics(),
                        Upgrades(),
                        ExoticTags(),
                        UpgradeTags(),
                        GlobalMemory(),
                        HasShield(),
                        HasPhase(),
                        MinimumCrew()
                    )
                        .forEach {
                            mutableDict!![it.key] = it
                        }
                }
                return mutableDict!!
            }

        fun getCondsFromJSONArray(arr: JSONArray): List<Condition> {
            val list: MutableList<Condition> = mutableListOf()
            for (i in 0 until arr.length()) {
                val effect = getCondFromJSONObject(arr.getJSONObject(i))
                list.add(effect)
            }
            return list
        }

        fun getCondFromJSONObject(obj: JSONObject): Condition {
            val effect = getCondFromDict(obj.getString("id"))
            effect.setup(obj)
            return effect
        }

        /**
         * returns a copy of the stat from the dict.
         */
        fun getCondFromDict(key: String): Condition {
            try {
                return getCopy(dict[key])!!
            } catch (ex: NullPointerException) {
                val logStr = "Condition for $key is missing."
                log.error(logStr)
                throw NullPointerException(logStr)
            }
        }

        fun <T> getCopy(obj: T): T {
            return obj!!::class.java.newInstance()
        }
    }
}