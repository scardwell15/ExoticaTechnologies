package exoticatechnologies.campaign.listeners

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.Pair
import exoticatechnologies.ETModSettings
import exoticatechnologies.campaign.market.MarketManager
import exoticatechnologies.hullmods.ExoticaTechHM
import exoticatechnologies.modifications.PersistentDataProvider.Companion.shipModificationMap
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModFactory.generateRandom
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.FleetMemberUtils
import exoticatechnologies.util.Utilities
import lombok.extern.log4j.Log4j
import org.apache.log4j.Logger
import kotlin.math.roundToInt


@Log4j
class CampaignEventListener(permaRegister: Boolean) : BaseCampaignEventListener(permaRegister), EveryFrameScript,
    EconomyTickListener {
    private val cleaningInterval = IntervalUtil(15f, 15f)

    override fun reportShownInteractionDialog(dialog: InteractionDialogAPI) {
        val interactionTarget = dialog.interactionTarget ?: return
        val plugin = dialog.plugin
        if (plugin is FleetInteractionDialogPluginImpl) {
            val context = plugin.context as FleetEncounterContext

            val battle: BattleAPI = context.battle
            val allFleets: MutableList<CampaignFleetAPI> = mutableListOf()
            allFleets.addAll(battle.bothSides ?: listOf())
            allFleets
                .filterNot { it == Global.getSector().playerFleet }
                .forEach {
                    if (activeFleets.contains(it)) {
                        return
                    }
                    dlog("Generating modifications for fleet.")
                    activeFleets.add(it)
                    applyExtraSystemsToFleet(it)
                }

            FireAll.fire(null, dialog, dialog.plugin.memoryMap, "GeneratedESForFleet")
        }

        val defenderFleet = interactionTarget.memoryWithoutUpdate.getFleet("\$defenderFleet")
        if (defenderFleet != null) {
            dlog("Defender fleet is in dialog. Generating modifications.")
            val newId = interactionTarget.id
            defenderFleet.id = newId
            applyExtraSystemsToFleet(defenderFleet)
            return
        }

        if (interactionTarget is CampaignFleetAPI) {
            dlog("Target fleet is in dialog.")
            if (activeFleets.contains(interactionTarget)) {
                return
            }
            dlog("Generating modifications for fleet.")
            activeFleets.add(interactionTarget)
            applyExtraSystemsToFleet(interactionTarget)
            FireAll.fire(null, dialog, dialog.plugin.memoryMap, "GeneratedESForFleet")
            return
        }

        if (interactionTarget.customPlugin is DerelictShipEntityPlugin) {
            //generate for interactionTarget.getId()
            if (Misc.getSalvageSpecial(interactionTarget) is ShipRecoverySpecialData) {
                return
            }
            if (interactionTarget.hasTag(Tags.UNRECOVERABLE)) {
                return
            }
            val plugin = interactionTarget.customPlugin as DerelictShipEntityPlugin
            val data: DerelictShipData = plugin.data
            val shipData: PerShipData = data.ship

            shipData.getVariant() ?: return

            val fm = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.getVariant())
            if (shipData.fleetMemberId == null) {
                shipData.fleetMemberId = fm.id
            } else {
                fm.id = shipData.fleetMemberId
            }
            if (shipData.shipName != null) {
                fm.shipName = shipData.shipName
            }

            fm.updateStats()
            val seed = shipData.fleetMemberId.hashCode().toLong()
            ShipModFactory.random.setSeed(seed)

            //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
            //it will never find one.
            val mods = generateRandom(fm)
            ShipModLoader.set(fm, fm.variant, mods)
            Global.getSector().addTransientScript(DerelictsEFScript(shipData.fleetMemberId, mods))
            return
        }

        if (Entities.DEBRIS_FIELD_SHARED == interactionTarget.customEntityType
            && interactionTarget.memoryWithoutUpdate.contains(MemFlags.SALVAGE_SPECIAL_DATA)
            && interactionTarget.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] is ShipRecoverySpecialData
        ) {

            val data = interactionTarget.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] as ShipRecoverySpecialData
            if (data.ships != null
                && data.ships.isNotEmpty()
            ) {

                val derelictVariantMap: MutableMap<String, ShipModifications> = LinkedHashMap()
                for (shipData in data.ships) {
                    val variant = shipData.getVariant() ?: continue
                    val fm = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant)
                    if (shipData.fleetMemberId != null) {
                        fm.id = shipData.fleetMemberId
                    } else {
                        shipData.fleetMemberId = fm.id
                    }
                    if (shipData.shipName != null) {
                        fm.shipName = shipData.shipName
                    }
                    val seed = shipData.fleetMemberId.hashCode().toLong()
                    ShipModFactory.random.setSeed(seed)
                    val mods = generateRandom(fm)
                    //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                    //it will never find one.
                    ShipModLoader.set(fm, fm.variant, mods)
                    derelictVariantMap[shipData.fleetMemberId] = mods
                }
                Global.getSector().addTransientScript(DerelictsEFScript(derelictVariantMap))
            }
        }
    }

    override fun reportFleetSpawned(fleet: CampaignFleetAPI) {
        if (fleet.isPlayerFleet) {
            return
        }
        if (Global.getSector().campaignUI.isShowingDialog) {
            dlog("Fleet spawned in dialog.")
            if (activeFleets.contains(fleet)) {
                return
            }
            activeFleets.add(fleet)
            applyExtraSystemsToFleet(fleet)
        }
    }

    override fun reportFleetDespawned(
        fleet: CampaignFleetAPI,
        reason: FleetDespawnReason,
        param: Any?
    ) {
        dlog(String.format("Fleet %s has despawned.", fleet.nameWithFaction))
        activeFleets.remove(fleet)
        removeExtraSystemsFromFleet(fleet)
    }

    override fun reportPlayerEngagement(result: EngagementResultAPI) {
        val otherResult = if (result.didPlayerWin()) result.loserResult else result.winnerResult
        val npcMembers: MutableList<FleetMemberAPI> = ArrayList()
        npcMembers.addAll(otherResult.disabled)
        npcMembers.addAll(otherResult.destroyed)

        for (member in npcMembers) {
            val mods = ShipModLoader.get(member, member.variant)
            if (mods != null) {
                mods.bandwidth *= (0.5f + 0.5f * ShipModFactory.random.nextFloat())

                mods.exotics.exoticData
                    .filter { (_, data) -> data.exotic.canDropFromCombat }
                    .filter { (_, data) -> ShipModFactory.random.nextFloat() >= data.exotic.getSalvageChance(8f) }
                    .forEach { (_, data) -> mods.removeExotic(data.exotic) }

                mods.getUpgradeMap()
                    .forEach { (upg, level) ->
                        val mult = (0.5f + 0.5f * ShipModFactory.random.nextFloat()) * (1 + upg.salvageChance)

                        mods.putUpgrade(upg, (mult.coerceAtMost(1f) * level).roundToInt().coerceAtLeast(1))
                    }
            }
        }

        val potentialDrops = getDrops(result, npcMembers)
        result.battle.getPrimary(result.battle.nonPlayerSide).memoryWithoutUpdate.set(
            "\$exotica_drops",
            potentialDrops,
            1f
        )

        val playerResult = if (result.didPlayerWin()) result.winnerResult else result.loserResult
        if (!ETModSettings.getBoolean(ETModSettings.SHIPS_KEEP_UPGRADES_ON_DEATH)) {
            val playerMembers: MutableList<FleetMemberAPI> = ArrayList()
            playerMembers.addAll(playerResult.disabled)
            playerMembers.addAll(playerResult.destroyed)
            for (member in playerMembers) {
                val mods = ShipModLoader.get(member, member.variant)
                if (mods != null) {
                    ExoticaTechHM.removeFromFleetMember(member)
                    ShipModLoader.remove(member, member.variant)
                }
            }
        }
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return FleetMemberUtils.moduleMap.isNotEmpty()
    }

    override fun advance(v: Float) {
        if (mergeCheck && !Global.getSector().campaignUI.isShowingMenu && Global.getSector().campaignUI.currentInteractionDialog == null) {
            mergeCheck = false
            Utilities.mergeChipsIntoCrate(Global.getSector().playerFleet.cargo)
        }

        if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.REFIT) {
            FleetMemberUtils.moduleMap.clear()
        }

        val checkFleets = false
        cleaningInterval.advance(v)
        if (checkFleets || cleaningInterval.intervalElapsed()) {
            cleaningInterval.elapsed = 0f
            checkNearbyFleets()

            //just in case, i guess.
            val fmIds = shipModificationMap.keys.toTypedArray()
            for (fmId in fmIds) {
                if (findFM(fmId) == null) {
                    shipModificationMap.remove(fmId)
                }
            }
        }

        if (!Global.getSector().hasTransientScript(DialogEFScript::class.java)) {
            Global.getSector().addTransientScript(DialogEFScript())
        }
    }

    override fun reportEconomyMonthEnd() {
        for (market in Global.getSector().economy.marketsCopy) {
            for (submarketId in submarketIdsToCheckForSpecialItems) {
                if (market.hasSubmarket(submarketId)) {
                    val submarket = market.getSubmarket(submarketId)
                    val cargo = submarket.cargoNullOk ?: continue
                    for (stack in cargo.stacksCopy) {
                        if (shouldRemoveStackFromSubmarket(stack)) {
                            cargo.removeStack(stack)
                        }
                    }
                }
            }
        }

        MarketManager.clearData()

        val report = SharedData.getData().previousReport
        val marketsNode = report.getNode(MonthlyReport.OUTPOSTS)
        val production = report.getNode(marketsNode, MonthlyReport.PRODUCTION)
        if (production.custom2 is CargoAPI) {
            val producedCargo: CargoAPI = production.custom2 as CargoAPI
            producedCargo.mothballedShips.membersListCopy.forEach {

                val pf = Global.getSector().playerFaction
                val prod = pf.production

                val gatheringPoint = prod.gatheringPoint ?: return@forEach
                ShipModFactory.generateForFleetMember(it, gatheringPoint)
            }
        }
    }

    companion object {
        private const val debug = false
        private val log = Logger.getLogger(Companion::class.java)
        private val submarketIdsToCheckForSpecialItems: MutableList<String> =
            mutableListOf(Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)
        val activeFleets: MutableList<CampaignFleetAPI> = ArrayList()
            get() = ArrayList(field).also { it.add(Global.getSector().playerFleet) }

        var mergeCheck = false

        private fun getDrops(
            result: EngagementResultAPI,
            members: List<FleetMemberAPI>
        ): Pair<Map<String, MutableMap<Int, Int>>, Map<ExoticData, Int>> {
            val upgradesMap: MutableMap<String, MutableMap<Int, Int>> = HashMap()
            val exotics: MutableMap<ExoticData, Int> = HashMap()
            for (member in members) {
                val mods = ShipModLoader.get(member, member.variant)
                if (mods != null) {
                    for ((upgrade, level) in mods.getUpgradeMap()) {
                        if (!upgradesMap.containsKey(upgrade.key)) {
                            upgradesMap[upgrade.key] = HashMap()
                        }
                        val perUpgradeMap = upgradesMap[upgrade.key]!!
                        if (!perUpgradeMap.containsKey(level)) {
                            perUpgradeMap[level] = 1
                        } else {
                            perUpgradeMap[level] = perUpgradeMap[level]!! + 1
                        }
                    }

                    for (exoticData in mods.getExoticSet()) {
                        val exotic = exoticData.exotic
                        if (!exotic.canDropFromCombat) {
                            continue
                        }
                        if (!exotics.containsKey(exoticData)) {
                            exotics[exoticData] = 1
                        } else {
                            exotics[exoticData] = exotics[exoticData]!! + 1
                        }
                    }
                }
            }
            return Pair(upgradesMap, exotics)
        }

        private fun shouldRemoveStackFromSubmarket(stack: CargoStackAPI): Boolean {
            if (stack.isSpecialStack && stack.specialDataIfSpecial != null) {
                val specialId = stack.specialDataIfSpecial.id
                if (specialId.startsWith("et_")) {
                    return true
                }
            }
            return false
        }

        fun findFM(fmId: String): FleetMemberAPI? {
            if (Global.getSector().playerFleet != null) {
                val fm = getFromFleet(fmId, Global.getSector().playerFleet.fleetData)
                if (fm != null) {
                    return fm
                }
            }

            return checkNearbyFleetsForFM(fmId) ?: checkStorageMarketsForFM(fmId)
        }

        private fun checkNearbyFleetsForFM(fmId: String): FleetMemberAPI? {
            return Global.getSector().currentLocation.fleets
                .map { getFromFleet(fmId, it.fleetData) }
                .firstOrNull()
        }

        private fun checkStorageMarketsForFM(fmId: String): FleetMemberAPI? {
            Global.getSector().allLocations
                .flatMap { it.allEntities }
                .filter { it.market != null }
                .flatMap { it.market.submarketsCopy }
                .map { it.cargoNullOk }
                .filterNotNull()
                .forEach { storage ->
                    var fm = getFromFleet(fmId, storage.mothballedShips)
                    if (fm != null) {
                        return fm
                    }
                    fm = getFromFleet(fmId, storage.fleetData)
                    if (fm != null) {
                        return fm
                    }
                }
            return null
        }

        private fun checkNearbyFleets() {
            var removedFleet = false
            val playerFleet = Global.getSector().playerFleet
            val it = activeFleets.iterator()
            while (it.hasNext()) {
                val fleet = it.next()
                if (fleet == playerFleet) {
                    continue
                }
                if (!fleet.isAlive
                    || fleet.containingLocation != playerFleet.containingLocation
                ) {
                    removedFleet = true
                }
                if (removedFleet) {
                    dlog(String.format("Fleet %s was not found in player location", fleet.nameWithFaction))
                    removeExtraSystemsFromFleet(fleet)
                    it.remove()
                }
            }
        }

        private fun removeExtraSystemsFromFleet(fleet: CampaignFleetAPI) {
            if (fleet.fleetData == null || fleet.fleetData.membersListCopy == null) {
                dlog("Fleet data was null")
                return
            }
            dlog(String.format("Removing mods for fleet %s", fleet.nameWithFaction))
            if (fleet == Global.getSector().playerFleet) {
                dlog("This fleet is the player fleet.")
                return
            }
            for (member in fleet.fleetData.membersListCopy) {
                if (!isInFleet(member.id, Global.getSector().playerFleet)) {
                    dlog(String.format("Removed mods for member %s", member.id))
                    ShipModLoader.remove(member, member.variant)
                }
            }
        }

        @JvmStatic
        fun applyExtraSystemsToFleet(fleet: CampaignFleetAPI) {
            val hash = fleet.id.hashCode()
            ShipModFactory.random.setSeed(hash.toLong())
            for (member in fleet.fleetData.membersListCopy) {
                if (member.isFighterWing) continue

                //generate random extra system
                val mods = generateRandom(member)
                ShipModLoader.set(member, member.variant, mods)
                ExoticaTechHM.addToFleetMember(member)
                dlog(String.format("Added modifications to member %s", member.shipName))
            }
        }

        private fun isInFleet(fm: FleetMemberAPI?, fleet: CampaignFleetAPI?): Boolean {
            if (fm == null || fleet == null) {
                return false
            }
            for (fleetMember in fleet.fleetData.membersListCopy) {
                if (fleetMember.isFighterWing) continue
                if (fm == fleetMember) {
                    return true
                }
            }
            return false
        }

        private fun isInFleet(fmId: String, fleet: CampaignFleetAPI?): Boolean {
            fleet?.let {
                return isInFleet(
                    fmId,
                    fleet.fleetData
                )
            }
            return false
        }

        private fun isInFleet(fmId: String, fleetData: FleetDataAPI): Boolean {
            return getFromFleet(fmId, fleetData) != null
        }

        private fun getFromFleet(fmId: String, fleetData: FleetDataAPI?): FleetMemberAPI? {
            if (fleetData != null) {
                for (fleetMember in fleetData.membersListCopy) {
                    if (fleetMember.isFighterWing) continue
                    if (fmId == fleetMember.id) {
                        return fleetMember
                    }
                }
            }
            return null
        }

        private fun dlog(format: String, vararg args: Any?) {
            if (!debug) return

            if (args.size == 0) {
                log.info(format)
            } else {
                val values = arrayOfNulls<String>(args.size)
                for (i in args.indices) {
                    values[i] = args[i].toString()
                }
                log.info(String.format(format, *values as Array<Any?>))
            }
        }
    }
}