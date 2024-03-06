package exoticatechnologies.campaign.rulecmd

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModFactory.generateModsForRecoveryData
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.util.StringUtils
import lombok.extern.log4j.Log4j

/**
 * vanilla does not have a way to add ES upgrades in a place where we can add options to the debris salvage dialog afterwards.
 * this script generates them for ships located in the debris field, and rules.csv is assumed to populate the option
 * for viewing them afterwards.
 */
@Log4j
class ETGenerateForDebrisField : BaseCommandPlugin() {
    override fun doesCommandAddOptions(): Boolean {
        return true
    }

    override fun getOptionOrder(params: List<Misc.Token>, memoryMap: Map<String, MemoryAPI>): Int {
        return 2
    }

    override fun execute(
        ruleId: String,
        dialog: InteractionDialogAPI?,
        params: List<Misc.Token>,
        memoryMap: Map<String, MemoryAPI>
    ): Boolean {
        if (dialog == null) return false
        val entity = dialog.interactionTarget
        var notableModsGenerated = false
        if (entity != null) {
            if (entity.customEntityType == Entities.DEBRIS_FIELD_SHARED && entity.memoryWithoutUpdate.contains(MemFlags.SALVAGE_SPECIAL_DATA)
                && entity.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] is ShipRecoverySpecialData
            ) {
                val data = entity.memoryWithoutUpdate[MemFlags.SALVAGE_SPECIAL_DATA] as ShipRecoverySpecialData
                generateModsForRecoveryData(data)
                notableModsGenerated = data.ships.any { ShipModLoader.getForSpecialData(it) != null }
            }
        }

        if (notableModsGenerated) {
            StringUtils.getTranslation("FleetScanner", "DebrisFieldHasNotableMods")
                .addToTextPanel(dialog.textPanel)
            dialog.optionPanel.addOption(
                StringUtils.getString("FleetScanner", "DebrisFieldScanOption"),
                "ETScanDebrisField"
            )
        }
        return false
    }
}