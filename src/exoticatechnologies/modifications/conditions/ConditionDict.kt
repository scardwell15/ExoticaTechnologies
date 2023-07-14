package exoticatechnologies.modifications.conditions

import exoticatechnologies.modifications.conditions.impl.*
import exoticatechnologies.modifications.stats.impl.UpgradeModEffectDict
import org.apache.log4j.Logger
import org.json.JSONArray
import org.json.JSONObject

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
                        MinimumCrew(),
                        CargoSpace(),
                        Hullmods(),
                        MissileSlots()
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
                val cond = dict[key]
                if (cond != null) {
                    return getCopy(cond)
                }

                val stat = UpgradeModEffectDict.getStatFromDict(key)
                return StatCondition(stat)
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