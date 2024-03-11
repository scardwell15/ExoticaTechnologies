package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.campaign.CargoStackAPI
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.list.ListPanel
import exoticatechnologies.ui2.list.ListPanelContext
import exoticatechnologies.ui2.util.chip.UpgradeChipSearcher
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet

class UpgradeChipListPanel(context: UpgradeChipListContext) :
    ListPanel<CargoStackAPI>(context) {
    override var itemHeight: Float = 96f
    override var renderBorder: Boolean = true
}

class UpgradeChipListContext(val upgrade: Upgrade, val exoticaContext: ExoticaPanelContext) :
    ListPanelContext<CargoStackAPI>() {
    override var listTitle: String = StringUtils.getString("Chips", "ChipsHeader")

    init {
        val chipSearcher = UpgradeChipSearcher()
        safeLet(exoticaContext.member, exoticaContext.mods, exoticaContext.fleet) { member, mods, fleet ->
            chipSearcher.getChips(fleet.cargo, member, mods, upgrade).forEach {
                listItems.add(UpgradeChipListItemContext(it, exoticaContext))
            }
        }
    }

    override fun sortItems(items: List<CargoStackAPI>): List<CargoStackAPI> {
        safeLet(exoticaContext.member, exoticaContext.variant, exoticaContext.mods) { member, variant, mods ->
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

        return super.sortItems(items)
    }

    private fun getBandwidthUsage(upgrade: Upgrade, level: Int): Float {
        return upgrade.bandwidthUsage * level
    }

    private fun getUpgrade(item: CargoStackAPI): Upgrade {
        return (item.plugin as UpgradeSpecialItemPlugin).upgrade!!
    }

    private fun getLevel(item: CargoStackAPI): Int {
        return (item.plugin as UpgradeSpecialItemPlugin).upgradeLevel
    }
}