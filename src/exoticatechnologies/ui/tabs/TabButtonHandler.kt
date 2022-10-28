package exoticatechnologies.ui.tabs

import exoticatechnologies.ui.ButtonHandler

open class TabButtonHandler(val parentPlugin: TabbedPanelUIPlugin, val plugin: TabPanelUIPlugin): ButtonHandler() {
    final override fun checked() {
        parentPlugin.pickedTab(plugin)
        clicked()
    }

    /**
     * Called after TabbedPanelUIPlugin listeners.
     */
    open fun clicked() {

    }
}