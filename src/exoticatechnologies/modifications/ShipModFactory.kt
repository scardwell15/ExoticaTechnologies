package exoticatechnologies.modifications

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import data.scripts.util.MagicSettings
import exoticatechnologies.ETModSettings
import exoticatechnologies.config.FactionConfig
import exoticatechnologies.config.FactionConfigLoader
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.ExoticsGenerator
import exoticatechnologies.modifications.upgrades.UpgradesGenerator
import exoticatechnologies.util.Utilities
import org.apache.log4j.Logger
import java.util.*

object ShipModFactory {
    val log: Logger = Logger.getLogger(ShipModFactory.javaClass)

    @JvmField
    val random = Random()

    @JvmStatic
    fun generateForFleetMember(fm: FleetMemberAPI): ShipModifications {
        var mods = ShipModLoader.get(fm)
        if (mods != null) {
            return mods
        }

        random.setSeed(fm.id.hashCode().toLong())

        mods = ShipModifications()
        mods.bandwidth = generateBandwidth(fm)

        ShipModLoader.set(fm, mods)
        return mods
    }

    private fun getFaction(fm: FleetMemberAPI): String? {
        if (fm.hullId.contains("ziggurat")) {
            return "omega"
        }

        if (fm.fleetData == null
            || fm.fleetData.fleet == null) {
            return null
        }

        try {
            if (fm.fleetData.fleet.memoryWithoutUpdate.contains("\$faction")) {
                return fm.fleetData.fleet.memoryWithoutUpdate["\$faction"] as String
            }
        } catch (th: Throwable) {
            return null
        }

        return fm.fleetData.fleet.faction?.id
    }

    @JvmStatic
    fun generateRandom(member: FleetMemberAPI): ShipModifications {
        val mods = ShipModLoader.get(member)
        if (mods != null) {
            return mods
        }

        return generateRandom(member, getFaction(member))
    }

    data class GenerationContext(
        var member: FleetMemberAPI,
        var mods: ShipModifications,
        var factionId: String?,
        var factionConfig: FactionConfig? = factionId?.let { return@let FactionConfigLoader.getFactionConfig(factionId) },
        var exoticChanceMult: Float = 1f,
        var upgradeChanceMult: Float = 1f)

    @JvmStatic
    fun generateRandom(member: FleetMemberAPI, faction: String?): ShipModifications {
        val mods = ShipModifications()
        val context = GenerationContext(member = member, mods = mods, factionId = faction)

        mods.bandwidth = generateBandwidth(member, faction)
        faction?.let {
            mods.exotics = ExoticsGenerator.generate(member, mods, context)
            mods.upgrades = UpgradesGenerator.generate(member, mods, context)
        }

        ShipModLoader.set(member, mods)

        return mods
    }

    fun generateBandwidth(member: FleetMemberAPI, faction: String?): Float {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH)
        }

        if (faction == Factions.OMEGA) {
            return 350f
        }

        val manufacturer = member.hullSpec.manufacturer
        var mult = 1.0f
        val manufacturerBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "manufacturerBandwidthMult")

        if (manufacturerBandwidthMult.containsKey(manufacturer)) {
            mult = manufacturerBandwidthMult[manufacturer]!!
        }

        faction?.let {
            val factionConfig = FactionConfigLoader.getFactionConfig(it)
            if (factionConfig.bandwidthMult != 1.0) {
                mult = factionConfig.bandwidthMult.toFloat()
            }
        }

        if (member.fleetData != null && member.fleetData.fleet != null) {
            if (member.fleetData.fleet.memoryWithoutUpdate.contains("\$exotica_bandwidthMult")) {
                mult *= member.fleetData.fleet.memoryWithoutUpdate.getFloat("\$exotica_bandwidthMult")
            }
        }

        mult += Utilities.getSModCount(member).toFloat()
        return Bandwidth.generate(mult).randomInRange
    }

    fun generateBandwidth(fm: FleetMemberAPI): Float {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH)
        }

        log.info(String.format("Generating bandwidth for fm ID [%s]", fm.id))

        val faction = getFaction(fm)
        return if (faction != null) {
            generateBandwidth(fm, faction)
        } else Bandwidth.generate().randomInRange
    }

    @JvmStatic
    fun getRandomNumberInRange(min: Float, max: Float): Float {
        return random.nextFloat() * (max - min) + min
    }

    @JvmStatic
    fun getRandomNumberInRange(min: Int, max: Int): Int {
        return if (min >= max) {
            if (min == max) min else random.nextInt(min - max + 1) + max
        } else {
            random.nextInt(max - min + 1) + min
        }
    }
}