package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListItemUIPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils

class UpgradeChipListItemUIPlugin(
    item: CargoStackAPI,
    member: FleetMemberAPI,
    val variant: ShipVariantAPI,
    val mods: ShipModifications,
    listPanel: ListUIPanelPlugin<CargoStackAPI>
) : ChipListItemUIPlugin(item, member, listPanel) {

    override fun showChip(rowPanel: CustomPanelAPI) {
        val upgrade = getUpgrade()

        val itemImage = rowPanel.createUIElement(iconSize, iconSize, false)
        itemImage.addImage(item.plugin.spec.iconName, iconSize, 0f)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
        itemInfo.addPara(item.displayName, 0f).position.inTL(3f, 3f)

        val creditCost: Int = ChipMethod.getCreditCost(member, mods, getUpgrade(), item)
        val hasCredits = Global.getSector().playerFleet.cargo.credits.get() > creditCost
        val creditsColor = if (hasCredits) null else Misc.getNegativeHighlightColor()

        StringUtils.getTranslation("CommonOptions", "CreditsPay")
            .format("credits", creditCost, creditsColor)
            .addToTooltip(itemInfo, itemInfo.prev)

        val upgradeBandwidth: Float = (getLevel() - mods.getUpgrade(upgrade)) * upgrade.bandwidthUsage
        val usableBandwidth = mods.getUsableBandwidth(member)
        val hasBandwidth = usableBandwidth >= upgradeBandwidth
        val bandwidthColor = if (hasBandwidth) Misc.getTextColor() else Misc.getNegativeHighlightColor()

        StringUtils.getTranslation("CommonOptions", "BandwidthUsedByUpgrade")
            .format("upgradeBandwidth", BandwidthUtil.getFormattedBandwidth(upgradeBandwidth), bandwidthColor)
            .addToTooltip(itemInfo, itemInfo.prev)

        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage, 3f)

        if (!hasCredits || !hasBandwidth) {
            setBGColor(red = 20, blue = 0, green = 0, alpha = 255)
            disabled = true
        }
    }

    override fun processInput(events: List<InputEventAPI>) {
        if (disabled) return
        if (isHovered(events)) {
            if (!wasHovered) {
                wasHovered = true
                setBGColor(alpha = 75)
            }
        } else if (wasHovered) {
            wasHovered = false
            setBGColor(alpha = 0)
        }
    }

    private fun getLevel(): Int {
        return (item.plugin as UpgradeSpecialItemPlugin).upgradeLevel
    }

    fun getUpgrade(): Upgrade {
        return (item.plugin as UpgradeSpecialItemPlugin).upgrade!!
    }
}