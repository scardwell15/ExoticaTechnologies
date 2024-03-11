package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui2.impl.mods.upgrades.methods.ChipMethod
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.list.ListItem
import exoticatechnologies.ui2.list.ListItemContext
import exoticatechnologies.ui2.list.ListPanelContext
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getFleetModuleSafe
import exoticatechnologies.util.safeLet
import java.awt.Color

class UpgradeChipListItem(context: UpgradeChipListItemContext): ListItem<CargoStackAPI>(context) {
    override fun decorate(menuPanel: CustomPanelAPI) {
        (currContext as? UpgradeChipListItemContext)?.let { context ->
            safeLet(context.member, context.mods) { member, mods ->
                val upgrade = getUpgrade()

                val iconSize = innerHeight / 2f
                val itemImage = menuPanel.createUIElement(iconSize, iconSize, false)
                itemImage.addImage(item.plugin.spec.iconName, iconSize, 0f)
                menuPanel.addUIElement(itemImage).inLMid(innerPadding)

                val itemInfo = menuPanel.createUIElement(innerWidth - iconSize, innerHeight, false)
                itemInfo.addPara("${upgrade.name} (${getLevel()})", 0f).position.inLMid(3f)

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

                StringUtils.getTranslation("Bandwidth", "BandwidthUsed")
                    .format("upgradeBandwidth", BandwidthUtil.getFormattedBandwidth(upgradeBandwidth), bandwidthColor)
                    .addToTooltip(itemInfo, itemInfo.prev)

                menuPanel.addUIElement(itemInfo).rightOfMid(itemImage, 3f)

                if (!hasCredits || !hasBandwidth) {
                    disabled = true
                    bgColor = Color(125, 0, 0, 255)
                    renderBackground = true
                }
            }
        }
    }

    private fun getLevel(): Int {
        return (currContext.item.plugin as UpgradeSpecialItemPlugin).upgradeLevel
    }

    fun getUpgrade(): Upgrade {
        return (currContext.item.plugin as UpgradeSpecialItemPlugin).upgrade!!
    }
}

class UpgradeChipListItemContext(item: CargoStackAPI, val exoticaContext: ExoticaPanelContext) : ListItemContext<CargoStackAPI>(item) {
    val member: FleetMemberAPI?
        get() = exoticaContext.member
    val variant: ShipVariantAPI?
        get() = exoticaContext.variant
    val mods: ShipModifications?
        get() = exoticaContext.mods
    val fleet: CampaignFleetAPI?
        get() = exoticaContext.member?.getFleetModuleSafe()
    val market: MarketAPI?
        get() = exoticaContext.market

    override fun createListItem(listContext: ListPanelContext<CargoStackAPI>): ListItem<CargoStackAPI> {
        return UpgradeChipListItem(this)
    }
}
