package exoticatechnologies.campaign.rulecmd

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModFactory

/**
 * vanilla does not have a way to add ES upgrades in a place where we can add options to the debris salvage dialog afterwards.
 * this script generates them for derelicts.
 */
class ETGenerateForDerelict : BaseCommandPlugin() {
    override fun doesCommandAddOptions(): Boolean {
        return false
    }

    override fun execute(
        ruleId: String,
        dialog: InteractionDialogAPI?,
        params: List<Misc.Token>,
        memoryMap: Map<String, MemoryAPI>
    ): Boolean {
        if (dialog == null) return false
        val entity = dialog.interactionTarget
        if (entity != null) {
            if (entity.customEntityType == Entities.WRECK && entity.memoryWithoutUpdate.contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && entity.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] is ShipRecoverySpecialData
            ) {
                val data = entity.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] as ShipRecoverySpecialData
                ShipModFactory.generateModsForRecoveryData(data)
            }
        }

        FireBest.fire(null, dialog, memoryMap, "ET_CreateScanOptionForDerelict true")

        return false
    }
}