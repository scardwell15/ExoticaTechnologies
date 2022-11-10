package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.bandwidth.BandwidthUtil
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.upgrades.methods.ChipMethod
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ChipListItemUIPlugin(item: CargoStackAPI,
                           var member: FleetMemberAPI,
                           var mods: ShipModifications,
                           private val listPanel: ListUIPanelPlugin<CargoStackAPI>
                        ) : ListItemUIPanelPlugin<CargoStackAPI>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    var wasHovered: Boolean = false
    override var panelWidth: Float = 64f
    override var panelHeight: Float = 64f

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val upgrade: Upgrade = getUpgrade()
        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        // Ship image with tooltip of the ship class
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

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, 0f)

        panel = rowPanel

        return panel!!
    }

    private fun getLevel(): Int {
        return (item.plugin as UpgradeSpecialItemPlugin).upgradeLevel
    }

    fun getUpgrade(): Upgrade {
        return (item.plugin as UpgradeSpecialItemPlugin).upgrade
    }

    override fun processInput(events: List<InputEventAPI>) {
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
}