package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListItemUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.ExoticPanelUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getMods

class ExoticChipListItemUIPlugin(
    item: CargoStackAPI,
    member: FleetMemberAPI,
    listPanel: ListUIPanelPlugin<CargoStackAPI>
) : ChipListItemUIPlugin(item, member, listPanel) {

    override fun showChip(rowPanel: CustomPanelAPI) {
        val mods = member.getMods()
        val plugin = getPlugin()
        val exotic = plugin.exoticData.exotic
        val type = plugin.exoticData.type

        val itemImage = rowPanel.createUIElement(iconSize, iconSize, false)
        itemImage.addImage(plugin.spec.iconName, iconSize, 0f)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
        itemInfo.addPara(exotic.name, plugin.exoticData.getColor(), 0f).position.inTL(3f, 3f)
        itemInfo.addPara(type.getName(), type.colorOverlay, 0f).position.inTL(3f, 3f)

        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage, 3f)
    }


    private fun getPlugin(): ExoticSpecialItemPlugin {
        return item.plugin as ExoticSpecialItemPlugin
    }
}