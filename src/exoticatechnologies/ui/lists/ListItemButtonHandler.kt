package exoticatechnologies.ui.lists

import exoticatechnologies.ui.ButtonHandler

class ListItemButtonHandler<T>(val rowPlugin: ListItemUIPanelPlugin<T>, val listPlugin: ListUIPanelPlugin<T>): ButtonHandler() {
    override fun checked() {
        if (rowPlugin.disabled) return
        listPlugin.itemClicked(rowPlugin.item)
    }
}