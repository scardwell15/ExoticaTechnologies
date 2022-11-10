package exoticatechnologies.modifications.stats.impl

import exoticatechnologies.modifications.stats.UpgradeModEffect
import exoticatechnologies.modifications.stats.impl.cr.CRLossRateEffect
import exoticatechnologies.modifications.stats.impl.cr.CRRecoveryRateEffect
import exoticatechnologies.modifications.stats.impl.cr.CRToDeployEffect
import exoticatechnologies.modifications.stats.impl.cr.PeakPerformanceTimeEffect
import exoticatechnologies.modifications.stats.impl.engines.*
import exoticatechnologies.modifications.stats.impl.flux.FluxCapacityEffect
import exoticatechnologies.modifications.stats.impl.flux.VentSpeedEffect
import exoticatechnologies.modifications.stats.impl.health.*
import exoticatechnologies.modifications.stats.impl.logistics.*
import exoticatechnologies.modifications.stats.impl.shield.*
import exoticatechnologies.modifications.stats.impl.weapons.*
import org.apache.log4j.Logger
import org.json.JSONArray
import org.json.JSONObject

abstract class UpgradeModEffectDict {
    companion object {
        private val log: Logger = Logger.getLogger(UpgradeModEffectDict::class.java)
        private var mutableDict: MutableMap<String, UpgradeModEffect>? = null
        private val dict: Map<String, UpgradeModEffect>
            get() {
                if (mutableDict == null) {
                    mutableDict = mutableMapOf()

                    listOf(
                        //cr
                        CRLossRateEffect(),
                        CRRecoveryRateEffect(),
                        CRToDeployEffect(),
                        PeakPerformanceTimeEffect(),
                        //engines
                        AccelerationEffect(),
                        BurnLevelEffect(),
                        DecelerationEffect(),
                        EngineHealthEffect(),
                        MaxSpeedEffect(),
                        TurnRateEffect(),
                        //flux
                        FluxCapacityEffect(),
                        VentSpeedEffect(),
                        //health
                        HullEffect(),
                        ArmorEffect(),
                        EMPDamageTakenEffect(),
                        ArmorDamageTakenEffect(),
                        HEDamageTakenEffect(),
                        FragDamageTakenEffect(),
                        KineticDamageTakenEffect(),
                        EnergyDamageTakenEffect(),
                        //logistics
                        CrewSalaryEffect(),
                        FuelUseEffect(),
                        MinimumCrewEffect(),
                        RepairRateAfterBattleEffect(),
                        SensorProfileEffect(),
                        SensorStrengthEffect(),
                        SuppliesPerMonthEffect(),
                        SuppliesToRecoverEffect(),
                        //shields
                        ShieldArcEffect(),
                        ShieldFluxPerDamEffect(),
                        ShieldTurnRateEffect(),
                        ShieldUnfoldRateEffect(),
                        ShieldUpkeepEffect(),
                        //weapons
                        MaxRecoilEffect(),
                        ProjectileSpeedEffect(),
                        RecoilPerShotEffect(),
                        WeaponFireRateEffect(),
                        BallisticFireRateEffect(),
                        WeaponFluxCostEffect(),
                        WeaponHealthEffect(),
                        WeaponTurnRateEffect(),
                    )
                        .map { it.key to it }
                        .forEach { (key, effect) ->
                            mutableDict!!.put(key, effect)
                        }
                }

                return mutableDict!!
            }

        fun getStatsFromJSONArray(arr: JSONArray): Map<String, UpgradeModEffect> {
            val map: LinkedHashMap<String, UpgradeModEffect> = linkedMapOf()
            for (i in 0 until arr.length()) {
                val effect = getStatFromJSONObj(arr.getJSONObject(i))
                map[effect.key] = effect
            }
            return map
        }

        fun getStatFromJSONObj(obj: JSONObject): UpgradeModEffect {
            val effect = getStatFromDict(obj.getString("id"))

            effect.baseEffect = obj.optFloat("baseEffect", 0f)
            effect.scalingEffect = obj.optFloat("scalingEffect", 0f)
            effect.startingLevel = obj.optInt("startingLevel", 1).coerceAtLeast(1)
            effect.hidden = obj.optBoolean("baseEffect", false)

            return effect
        }

        /**
         * returns a copy of the stat from the dict.
         */
        fun getStatFromDict(key: String): UpgradeModEffect {
            try {
                return getCopy(dict[key])!!
            } catch (ex: NullPointerException) {
                val logStr = "UpgradeModEffect for $key is missing."
                log.error(logStr)
                throw NullPointerException(logStr)
            }
        }

        fun <T> getCopy(obj: T): T {
            return obj!!::class.java.newInstance()
        }
    }
}

private fun JSONObject.optFloat(s: String, fl: Float): Float {
    return this.optDouble(s, fl.toDouble()).toFloat()
}
