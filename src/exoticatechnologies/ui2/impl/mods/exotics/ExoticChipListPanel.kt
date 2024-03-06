package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.list.ListPanel
import exoticatechnologies.ui2.list.ListPanelContext
import exoticatechnologies.ui2.util.chip.ExoticChipSearcher
import exoticatechnologies.ui2.util.chip.UpgradeChipSearcher
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet

class ExoticChipListPanel(context: ExoticChipListContext) :
    ListPanel<CargoStackAPI>(context) {
    override var itemHeight: Float = 64f
    override var renderBorder: Boolean = true
}

class ExoticChipListContext(val exotic: Exotic, val exoticaContext: ExoticaPanelContext) :
    ListPanelContext<CargoStackAPI>() {
    override var listTitle: String = StringUtils.getString("ExoticsDialog", "ChipsHeader")
    private val market: MarketAPI? = exoticaContext.market

    init {
        val chipSearcher = ExoticChipSearcher()
        safeLet(exoticaContext.member, exoticaContext.mods, exoticaContext.fleet) { member, mods, fleet ->
            chipSearcher.getChips(fleet.cargo, member, mods, exotic).forEach {
                listItems.add(ExoticChipListItemContext(it, exoticaContext))
            }
        }
    }

    override fun sortItems(items: List<CargoStackAPI>): List<CargoStackAPI> {
        safeLet(exoticaContext.member, exoticaContext.variant, exoticaContext.mods) { member, variant, mods ->
            val usableBandwidth = mods.getUsableBandwidth(member)
            return items.sortedWith { stackA, stackB ->
                val a = getExotic(stackA).exotic
                val b = getExotic(stackB).exotic
                if (a == null)
                    if (b == null)
                        0
                    else
                        1
                else if (b ==  null)
                    -1
                else
                    if (mods.hasExotic(a))
                        if (mods.hasExotic(b))
                            0
                        else
                            -1
                    else if (mods.hasExotic(b))
                        1
                    else
                        if (a.canAfford(Global.getSector().playerFleet, market))
                            if (b.canAfford(Global.getSector().playerFleet, market))
                                0
                            else
                                -1
                        else if (b.canAfford(Global.getSector().playerFleet, market))
                            1
                        else
                            if (a.canApply(member, variant, mods))
                                if (b.canApply(member, variant, mods))
                                    0
                                else
                                    -1
                            else if (b.canApply(member, variant, mods))
                                1
                            else
                                0
            }
        }

        return super.sortItems(items)
    }

    private fun getExotic(item: CargoStackAPI): ExoticSpecialItemPlugin {
        return item.plugin as ExoticSpecialItemPlugin
    }
}