package exoticatechnologies.ui.tabs

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.InteractiveUIPanelPlugin

abstract class TabPanelUIPlugin : InteractiveUIPanelPlugin() {
    protected var tabPlugin: TabButtonUIPlugin? = null
    abstract val tabText: String

    abstract fun layoutPanel(holdingPanel: CustomPanelAPI, parentPlugin: TabbedPanelUIPlugin): TooltipMakerAPI?

    protected open fun getNewTabButtonUIPlugin(): TabButtonUIPlugin {
        return TabButtonUIPlugin(tabText)
    }

    fun getTabButtonUIPlugin(): TabButtonUIPlugin {
        if (tabPlugin == null) {
            tabPlugin = getNewTabButtonUIPlugin()
        }
        return tabPlugin!!
    }

    fun getNewButtonHandler(parentPlugin: TabbedPanelUIPlugin): TabButtonHandler {
        return TabButtonHandler(parentPlugin, this)
    }

    fun activated(plugin: TabbedPanelUIPlugin) {
        tabPlugin!!.activated(plugin)
    }

    fun deactivated(plugin: TabbedPanelUIPlugin) {
        tabPlugin!!.deactivated(plugin)
    }
}