package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.tabs.FilteredTabListContext
import exoticatechnologies.ui2.tabs.FilteredTabListPanel
import exoticatechnologies.ui2.tabs.TabListContext
import exoticatechnologies.util.safeLet

class UpgradesListPanel(context: UpgradesListContext) :
    FilteredTabListPanel<Upgrade, ExoticaPanelContext>(context) {
    override fun getFilters(): List<String>? {
        return UpgradesHandler.UPGRADES_BY_HINT.keys.filterNot { it.isEmpty() }.toList()
    }

    override fun getFiltersFromItem(item: Upgrade): List<String> {
        return item.hints
    }

    override fun refresh(menuPanel: CustomPanelAPI, context: TabListContext<Upgrade, ExoticaPanelContext>) {
        super.refresh(menuPanel, context)

        (context as UpgradesListContext).mods?.addListener("${this::class}") {
            this.refreshPanel()
        }
    }
}


class UpgradesListContext(exoticaContext: ExoticaPanelContext) :
    FilteredTabListContext<Upgrade, ExoticaPanelContext>() {
    private val member = exoticaContext.member
    private val variant = exoticaContext.variant
    private val fleet = exoticaContext.fleet
    val mods = exoticaContext.mods
    private val market = exoticaContext.market

    init {
        safeLet(member, mods) { member, mods ->
            UpgradesHandler.UPGRADES_LIST.forEach {
                if (it.shouldShow(member, mods, market) || mods.hasUpgrade(it))
                    tabs.add(UpgradeItemContext(it, exoticaContext))
            }
        }
    }

    override fun sortItems(items: List<Upgrade>): List<Upgrade> {
        safeLet(member, variant, mods) { member, variant, mods ->
            return items.sortedWith(compareBy(
                { a -> if (mods.hasUpgrade(a)) -mods.getUpgrade(a) else 0 },
                { a -> !a.canApplyImpl(member, variant, mods) },
                { a -> a.name }
            ))
        }
        return items
    }
}
