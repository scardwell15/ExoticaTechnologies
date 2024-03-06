package exoticatechnologies.ui2.impl.mods

import exoticatechnologies.ui2.impl.mods.exotics.ExoticsTabContext
import exoticatechnologies.ui2.impl.mods.overview.OverviewTabContext
import exoticatechnologies.ui2.impl.mods.upgrades.UpgradesTabContext

object ModMenuManager {
    @JvmStatic
    val modMenuPanels: MutableList<ModTabContext> = mutableListOf()

    @JvmStatic
    fun addMenu(plugin: ModTabContext) {
        modMenuPanels.add(plugin)
    }

    init {
        addMenu(OverviewTabContext())
        addMenu(UpgradesTabContext())
        addMenu(ExoticsTabContext())
    }
}
