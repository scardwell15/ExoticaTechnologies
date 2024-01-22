package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.exotics.types.ExoticTypeTooltip
import exoticatechnologies.ui.impl.shop.chips.ChipListItemUIPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import org.magiclib.kotlin.setAlpha

class ExoticChipListItemUIPlugin(
    item: CargoStackAPI,
    member: FleetMemberAPI,
    val variant: ShipVariantAPI,
    val mods: ShipModifications,
    listPanel: ListUIPanelPlugin<CargoStackAPI>
) : ChipListItemUIPlugin(item, member, listPanel) {

    private var itemImage: TooltipMakerAPI? = null
    private val exoticData: ExoticData
        get() = getPlugin().exoticData!!

    override fun showChip(rowPanel: CustomPanelAPI) {
        val plugin = getPlugin()
        val exotic = exoticData.exotic
        val type = exoticData.type

        itemImage = rowPanel.createUIElement(iconSize, iconSize, false)
        itemImage!!.addImage(plugin.spec.iconName, iconSize, 0f)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
        itemInfo.addPara(exotic.name, exoticData.getColor(), 0f).position.inTL(3f, 3f)
        itemInfo.addPara(type.name, type.colorOverlay.setAlpha(255), 0f)


        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage, 3f)
    }

    override fun addedChip(tooltip: TooltipMakerAPI, rowPanel: CustomPanelAPI) {
        ExoticTypeTooltip.addToPrev(tooltip, member, mods, exoticData.type)
    }

    private fun getPlugin(): ExoticSpecialItemPlugin {
        return item.plugin as ExoticSpecialItemPlugin
    }
}