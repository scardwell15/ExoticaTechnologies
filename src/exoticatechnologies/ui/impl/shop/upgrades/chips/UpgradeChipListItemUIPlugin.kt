package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListItemUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getMods

class UpgradeChipListItemUIPlugin(
    item: CargoStackAPI,
    member: FleetMemberAPI,
    listPanel: ListUIPanelPlugin<CargoStackAPI>
) : ChipListItemUIPlugin(item, member, listPanel) {

    override fun showChip(rowPanel: CustomPanelAPI) {
        val mods = member.getMods()
        val upgrade = getUpgrade()

        val itemImage = rowPanel.createUIElement(iconSize, iconSize, false)
        itemImage.addImage(item.plugin.spec.iconName, iconSize, 0f)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
        itemInfo.addPara(item.displayName, 0f).position.inTL(3f, 3f)

        val creditCost: Int = ChipMethod.getCreditCost(member, mods, getUpgrade(), item)
        StringUtils.getTranslation("CommonOptions", "CreditsPay")
            .format("credits", creditCost)
            .addToTooltip(itemInfo, itemInfo.prev)

        val upgradeBandwidth: Float = (getLevel() - mods.getUpgrade(upgrade)) * upgrade.bandwidthUsage
        StringUtils.getTranslation("CommonOptions", "BandwidthUsedByUpgrade")
            .format("upgradeBandwidth", BandwidthUtil.getFormattedBandwidth(upgradeBandwidth))
            .addToTooltip(itemInfo, itemInfo.prev)

        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage, 3f)
    }


    private fun getLevel(): Int {
        return (item.plugin as UpgradeSpecialItemPlugin).upgradeLevel
    }

    fun getUpgrade(): Upgrade {
        return (item.plugin as UpgradeSpecialItemPlugin).upgrade!!
    }
}