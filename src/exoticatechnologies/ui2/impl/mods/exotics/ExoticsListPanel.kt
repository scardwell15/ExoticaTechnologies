package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.tabs.FilteredTabListContext
import exoticatechnologies.ui2.tabs.FilteredTabListPanel
import exoticatechnologies.ui2.tabs.TabListContext
import exoticatechnologies.util.safeLet

class ExoticsListPanel(context: ExoticsListContext) :
    FilteredTabListPanel<Exotic, ExoticaPanelContext>(context) {
    override fun getFilters(): List<String>? {
        return ExoticsHandler.EXOTICS_BY_HINT.keys.filterNot { it.isEmpty() }.toList()
    }

    override fun getFiltersFromItem(item: Exotic): List<String> {
        return item.hints
    }

    override fun refresh(menuPanel: CustomPanelAPI, context: TabListContext<Exotic, ExoticaPanelContext>) {
        super.refresh(menuPanel, context)

        (context as ExoticsListContext).mods?.addListener("${this::class}") {
            listItems
                .filter { this.shouldAllowItem(it.item) }
                .forEach {
                    if (it.getPanel() == null) {
                        listInnerPanel?.let { innerPanel ->
                            it.layoutPanel(innerPanel, ExoticItemContext(it.item, it.panelContext))
                        }
                    } else {
                        it.refreshPanel()
                    }
                }
            super.finishedRefresh(menuPanel, context, listItems)
        }
    }
}


class ExoticsListContext(exoticaContext: ExoticaPanelContext) :
    FilteredTabListContext<Exotic, ExoticaPanelContext>() {
    private val member = exoticaContext.member
    private val variant = exoticaContext.variant
    private val fleet = exoticaContext.fleet
    val mods = exoticaContext.mods
    private val market = exoticaContext.market

    init {
        safeLet(member, mods) { member, mods ->
            ExoticsHandler.EXOTIC_LIST.forEach {
                if (it.shouldShow(member, mods, market) || mods.hasExotic(it))
                    tabs.add(ExoticItemContext(it, exoticaContext))
            }
        }
    }

    override fun sortItems(items: List<Exotic>): List<Exotic> {
        safeLet(member, variant, mods, fleet) { member, variant, mods, fleet ->
            return items.sortedWith(compareBy(
                { a -> !mods.hasExotic(a) },
                { a -> !a.canAfford(fleet, market) },
                { a -> !a.canApplyImpl(member, variant, mods) },
                { a -> a.name }
            ))
        }
        return items
    }
}
