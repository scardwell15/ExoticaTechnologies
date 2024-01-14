package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.filtered.FilteredListPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeListUIPlugin(
    parentPanel: CustomPanelAPI,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var mods: ShipModifications,
    var market: MarketAPI?
): FilteredListPanelPlugin<Upgrade>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "OpenUpgradeOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)
    private var modsValue: Float = mods.getValue()

    override fun advancePanel(amount: Float) {
        val newValue = mods.getValue()
        if (modsValue != newValue) {
            modsValue = newValue
            layoutPanels()
        }
    }

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Upgrade): ListItemUIPanelPlugin<Upgrade> {
        val rowPlugin = UpgradeListItemUIPlugin(item, member, variant, mods, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<Upgrade>): List<Upgrade> {
        return items.sortedWith { a, b ->
            if (mods.hasUpgrade(a))
                if (mods.hasUpgrade(b))
                    mods.getUpgrade(a) - mods.getUpgrade(b)
                else
                    -1
            else
                if (mods.hasUpgrade(b))
                    1
                else if (a.canApply(member, mods))
                    if (b.canApply(member, mods))
                        0
                    else
                        -1
                else if (b.canApply(member, mods))
                    1
            else
                0
        }
    }

    override fun shouldMakePanelForItem(item: Upgrade): Boolean {
        if (!super.shouldMakePanelForItem(item)) return false

        if (mods.hasUpgrade(item)) {
            return true
        }

        if (member.shipName == null && !item.shouldAffectModule(null, null)) return false

        if (market == null) {
            return false
        }
        
        return item.shouldShow(member, mods, market)
    }

    override fun getFilters(): List<String>? {
        return UpgradesHandler.UPGRADES_BY_HINT.keys.filterNot { it.isEmpty() }.toList()
    }

    override fun getFiltersFromItem(item: Upgrade): List<String> {
        return item.hints
    }
}