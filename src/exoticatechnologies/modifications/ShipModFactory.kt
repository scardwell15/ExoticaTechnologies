package exoticatechnologies.modifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData
import exoticatechnologies.ETModSettings
import exoticatechnologies.campaign.listeners.DerelictsEFScript
import exoticatechnologies.config.FactionConfig
import exoticatechnologies.config.FactionConfigLoader
import exoticatechnologies.modifications.bandwidth.Bandwidth
import exoticatechnologies.modifications.exotics.ExoticsGenerator
import exoticatechnologies.modifications.upgrades.UpgradesGenerator
import exoticatechnologies.util.Utilities
import exoticatechnologies.util.getFleetModuleSafe
import org.apache.log4j.Logger
import org.magiclib.util.MagicSettings
import java.util.*

object ShipModFactory {
    val log: Logger = Logger.getLogger(ShipModFactory.javaClass)

    @JvmField
    val random = Random()

    @JvmStatic
    fun generateForFleetMember(member: FleetMemberAPI, variant: ShipVariantAPI = member.variant): ShipModifications {
        var mods = ShipModLoader.get(member, variant)
        if (mods != null) {
            return mods
        }

        random.setSeed(member.id.hashCode().toLong())

        mods = ShipModifications()
        mods.bandwidth = generateBandwidth(member)

        ShipModLoader.set(member, variant, mods)
        return mods
    }

    @JvmStatic
    fun generateForFleetMember(member: FleetMemberAPI, market: MarketAPI): ShipModifications {
        var mods = ShipModLoader.get(member, member.variant)
        if (mods != null) {
            return mods
        }

        random.setSeed(member.id.hashCode().toLong())

        mods = ShipModifications()
        mods.bandwidth = generateBandwidth(member, market)

        ShipModLoader.set(member, member.variant, mods)
        return mods
    }

    private fun getFaction(fm: FleetMemberAPI): String? {
        if (fm.hullId.contains("ziggurat")) {
            return "omega"
        }

        val fleet: CampaignFleetAPI = fm.getFleetModuleSafe() ?: return null
        var faction = fleet.faction?.id

        try {
            if (fleet.memoryWithoutUpdate.contains("\$faction")) {
                faction = fleet.memoryWithoutUpdate["\$faction"] as String
            }
        } catch (th: Throwable) {
            return null
        }

        Global.getSector().intelManager.getIntel(PersonBountyIntel::class.java)
            .map { it as PersonBountyIntel }
            .filter { it.bountyType == PersonBountyIntel.BountyType.DESERTER && it.fleet == fleet && it.faction != null }
            .randomOrNull()
            ?.let { bounty ->
                faction = bounty.faction.id
            }

        return faction
    }

    @JvmStatic
    fun generateRandom(member: FleetMemberAPI, variant: ShipVariantAPI = member.variant): ShipModifications {
        val mods = ShipModLoader.get(member, variant)
        if (mods != null) {
            return mods
        }

        return generateRandom(member, getFaction(member), variant)
    }

    data class GenerationContext(
        var member: FleetMemberAPI,
        var variant: ShipVariantAPI = member.variant,
        var mods: ShipModifications = ShipModifications(),
        var factionId: String? = member.getFleetModuleSafe()?.faction?.id,
        var exoticChanceMult: Float = 1f,
        var upgradeChanceMult: Float = 1f,
        var bandwidthMult: Float = 1f) {

        val factionConfig: FactionConfig?
            get() = factionId?.let { return@let FactionConfigLoader.getFactionConfig(factionId!!) }
        val fleet: CampaignFleetAPI?
            get() = member.getFleetModuleSafe()
    }

    @JvmStatic
    fun generateRandom(member: FleetMemberAPI, faction: String?, variant: ShipVariantAPI = member.variant): ShipModifications {
        if (faction == Global.getSector().playerFaction.id) {
            println("generateRandom was just used on a player faction ship, which should not happen.")
        }
        val mods = ShipModifications()
        val context = GenerationContext(member = member, variant = variant, mods = mods, factionId = faction)

        mods.bandwidth = generateBandwidth(member, faction, context)
        faction?.let {
            mods.exotics = ExoticsGenerator.generate(context)
            mods.upgrades = UpgradesGenerator.generate(context)
        }

        ShipModLoader.set(member, member.variant, mods)

        return mods
    }

    /**
     *
     */
    @JvmStatic
    fun generateRandom(context: GenerationContext): ShipModifications {
        val faction = context.factionId
        if (faction == Global.getSector().playerFaction.id) {
            println("generateRandom was just used on a player faction ship, which should not happen.")
        }

        val mods = ShipModifications()
        mods.bandwidth = generateBandwidth(context.member, faction, context)
        faction?.let {
            mods.exotics = ExoticsGenerator.generate(context)
            mods.upgrades = UpgradesGenerator.generate(context)
        }

        ShipModLoader.set(context.member, context.variant, mods)

        return mods
    }

    fun generateBandwidth(member: FleetMemberAPI, faction: String?, context: GenerationContext?): Float {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH)
        }

        if (faction == Factions.OMEGA) {
            return Bandwidth.UNKNOWN.bandwidth
        }


        var mult = 1.0f
        if (faction != Factions.PLAYER) {
            mult *= 1.5f
        }

        val manufacturer = member.hullSpec.manufacturer
        val manufacturerBandwidthMult = MagicSettings.getFloatMap("exoticatechnologies", "manufacturerBandwidthMult")
        if (manufacturerBandwidthMult.containsKey(manufacturer)) {
            mult *= manufacturerBandwidthMult[manufacturer]!!
        }

        faction?.let {
            val factionConfig = FactionConfigLoader.getFactionConfig(it)
            if (factionConfig.bandwidthMult != 1.0) {
                mult *= factionConfig.bandwidthMult.toFloat()
            }
        }

        if (member.fleetData != null && member.fleetData.fleet != null) {
            if (member.fleetData.fleet.memoryWithoutUpdate.contains("\$exotica_bandwidthMult")) {
                mult *= member.fleetData.fleet.memoryWithoutUpdate.getFloat("\$exotica_bandwidthMult")
            }
        }

        context?.let {
            mult *= context.bandwidthMult
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
            generateBandwidth(fm, faction, null)
        } else Bandwidth.generate().randomInRange
    }

    fun generateBandwidth(member: FleetMemberAPI, market: MarketAPI): Float {
        if (!ETModSettings.getBoolean(ETModSettings.RANDOM_BANDWIDTH)) {
            return ETModSettings.getFloat(ETModSettings.STARTING_BANDWIDTH)
        }

        log.info(String.format("Generating bandwidth for fm ID [%s] at market [%s]", member.id, market.name))

        val faction = market.factionId
        if (faction == Factions.OMEGA) {
            return Bandwidth.MAX_BANDWIDTH
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

        mult += market.industries
            .mapNotNull { ETModSettings.getProductionBandwidthMults()[it.id] }
            .sum()

        return Bandwidth.generate(mult).randomInRange
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

    @JvmStatic
    fun createGenerationContextForMemberAndVariant(member: FleetMemberAPI, variant: ShipVariantAPI): GenerationContext {
        var faction: String? =  member.getFleetModuleSafe()?.faction?.id

        if (faction == null) {
            val validFactions = Global.getSector().allFactions
                .filter { it.knowsShip(member.hullId) }
                .map { translateFactionId(it.id) }
                .toSet()

            faction = validFactions.randomOrNull()
        }

        val generationContext = GenerationContext(member)
        generationContext.factionId = faction
        return generationContext
    }

    @JvmStatic
    fun translateFactionId(factionId: String): String {
        if (factionId.equals("domain") || factionId.equals("sector") || factionId.equals("domain")) {
            return "independent"
        }
        return factionId
    }

    @JvmStatic
    fun generateModsForDerelict(plugin: DerelictShipEntityPlugin) {
        val data: DerelictShipEntityPlugin.DerelictShipData = plugin.data
        data.ship?.let {
            generateModsForPerShipData(it)
        }
    }

    @JvmStatic
    fun generateModsForRecoveryData(data: ShipRecoverySpecial.ShipRecoverySpecialData) {
        data.ships?.forEach { generateModsForPerShipData(it) }
    }

    @JvmStatic
    fun generateModsForPerShipData(shipData: PerShipData) {
        shipData.getVariant() ?: return
        if (ShipModLoader.getForSpecialData(shipData) != null) return

        val derelictVariant = shipData.getVariant()
        val member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, derelictVariant)
        if (shipData.fleetMemberId == null) {
            shipData.fleetMemberId = member.id
        } else {
            member.id = shipData.fleetMemberId
        }
        if (shipData.shipName != null) {
            member.shipName = shipData.shipName
        }

        member.setVariant(derelictVariant, false, false)
        member.updateStats()

        if (ShipModLoader.get(member, derelictVariant) != null) return

        val seed = shipData.fleetMemberId.hashCode().toLong()
        random.setSeed(seed)

        val generationContext = createGenerationContextForMemberAndVariant(member, derelictVariant)
        generationContext.upgradeChanceMult = 3f
        generationContext.exoticChanceMult = 3f

        //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
        //it will never find one.
        val mods = generateRandom(generationContext)
        ShipModLoader.set(member, derelictVariant, mods)
        Global.getSector().addTransientScript(DerelictsEFScript(shipData.fleetMemberId, mods))
    }
}