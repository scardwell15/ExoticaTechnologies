package exoticatechnologies.ui.impl.shop.upgrades.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListUIPlugin
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeChipListUIPlugin(
    parentPanel: CustomPanelAPI,
    member: FleetMemberAPI,
    val variant: ShipVariantAPI,
    val mods: ShipModifications
) : ChipListUIPlugin(parentPanel, member) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "UpgradeChipsHeader").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(
        tooltip: TooltipMakerAPI,
        item: CargoStackAPI
    ): ListItemUIPanelPlugin<CargoStackAPI> {
        val rowPlugin = UpgradeChipListItemUIPlugin(item, member, variant, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<CargoStackAPI>): List<CargoStackAPI> {
        val usableBandwidth = mods.getUsableBandwidth(member)
        return items.sortedWith { stackA, stackB ->
            val a = getUpgrade(stackA)
            val b = getUpgrade(stackB)
            if (a.canApply(member, variant, mods))
                if (b.canApply(member, variant, mods)) {
                    val aLevel = getLevel(stackA)
                    val bLevel = getLevel(stackB)
                    val aHasBandwidth = getBandwidthUsage(a, aLevel) <= usableBandwidth
                    val bHasBandwidth = getBandwidthUsage(b, bLevel) <= usableBandwidth
                    if (aHasBandwidth)
                        if (bHasBandwidth)
                            getLevel(stackB) - getLevel(stackA)
                        else
                            -1
                    else if (bHasBandwidth)
                        1
                    else
                        0
                } else
                    -1
            else if (b.canApply(member, variant, mods))
                1
            else
                0
        }
    }

    fun getBandwidthUsage(upgrade: Upgrade, level: Int): Float {
        return upgrade.bandwidthUsage * level
    }

    fun getUpgrade(item: CargoStackAPI): Upgrade {
        return (item.plugin as UpgradeSpecialItemPlugin).upgrade!!
    }

    fun getLevel(item: CargoStackAPI): Int {
        return (item.plugin as UpgradeSpecialItemPlugin).upgradeLevel
    }
}