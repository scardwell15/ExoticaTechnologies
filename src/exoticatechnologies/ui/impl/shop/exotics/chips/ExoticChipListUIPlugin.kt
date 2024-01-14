package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListUIPlugin
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticChipListUIPlugin(
    parentPanel: CustomPanelAPI,
    member: FleetMemberAPI,
    val variant: ShipVariantAPI,
    val mods: ShipModifications,
    val market: MarketAPI
) : ChipListUIPlugin(parentPanel, member) {
    override val listHeader = StringUtils.getTranslation("ExoticsDialog", "ChipsHeader").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(
        tooltip: TooltipMakerAPI,
        item: CargoStackAPI
    ): ListItemUIPanelPlugin<CargoStackAPI> {
        val rowPlugin = ExoticChipListItemUIPlugin(item, member, variant, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<CargoStackAPI>): List<CargoStackAPI> {
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

    private fun getExotic(item: CargoStackAPI): ExoticSpecialItemPlugin {
        return item.plugin as ExoticSpecialItemPlugin
    }
}