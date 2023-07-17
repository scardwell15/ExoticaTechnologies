package exoticatechnologies.campaign.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.campaign.listeners.DerelictsEFScript
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModFactory.generateRandom
import exoticatechnologies.modifications.ShipModLoader.Companion.getForSpecialData
import exoticatechnologies.modifications.ShipModLoader.Companion.set
import exoticatechnologies.modifications.ShipModifications
import org.apache.log4j.Logger

/**
 * vanilla does not have a way to add ES upgrades in a place where we can add options to the debris salvage dialog afterwards.
 * this script generates them for derelicts.
 */
class ETGenerateForDerelict : BaseCommandPlugin() {
    companion object {
        val log = Logger.getLogger(ETGenerateForDerelict.javaClass)
    }
    override fun doesCommandAddOptions(): Boolean {
        return false
    }

    override fun execute(
        ruleId: String,
        dialog: InteractionDialogAPI,
        params: List<Misc.Token>,
        memoryMap: Map<String, MemoryAPI>
    ): Boolean {
        if (dialog == null) return false
        val entity = dialog.interactionTarget
        val notableModsGenerated = false
        if (entity != null) {
            if (entity.customEntityType == Entities.WRECK && entity.memoryWithoutUpdate.contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && entity.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] is ShipRecoverySpecialData
            ) {
                val data = entity.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] as ShipRecoverySpecialData
                if (data.ships != null
                    && !data.ships.isEmpty()
                ) {
                    val derelictVariantMap: MutableMap<String, ShipModifications> = LinkedHashMap()
                    for (i in data.ships.indices) {
                        val shipData = data.ships[i]
                        if (getForSpecialData(shipData) != null) continue
                        if (shipData.getVariant() == null) continue
                        val member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipData.getVariant())
                        if (shipData.fleetMemberId == null) {
                            shipData.fleetMemberId = member.id
                        } else {
                            member.id = shipData.fleetMemberId
                        }
                        log.info("derelict: generating for fmId " + shipData.fleetMemberId)

                        //note: saving here isn't really an issue because the cleanup script searches for fleet members with this ID.
                        //it will never find one.
                        ShipModFactory.random.setSeed(shipData.fleetMemberId.hashCode().toLong())
                        val mods = generateRandom(member)
                        set(member, member.variant, mods)
                        derelictVariantMap[shipData.fleetMemberId.hashCode().toString()] = mods
                    }
                    Global.getSector().addTransientScript(DerelictsEFScript(derelictVariantMap))
                }
            }
        }

        FireBest.fire(null, dialog, memoryMap, "ET_CreateScanOptionForDerelict true")

        return false
    }
}