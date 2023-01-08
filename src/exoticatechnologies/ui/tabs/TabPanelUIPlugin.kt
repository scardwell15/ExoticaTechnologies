package exoticatechnologies.ui.tabs

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.BaseUIPanelPlugin

abstract class TabPanelUIPlugin: BaseUIPanelPlugin() {
    protected var tabPlugin: TabButtonUIPlugin? = null
    protected var tabText: String = "TabText"

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