package exoticatechnologies.ui.lists

import exoticatechnologies.ui.ButtonHandler

class ListItemButtonHandler<T>(val rowPlugin: ListItemUIPanelPlugin<T>, val listPlugin: ListUIPanelPlugin<T>): ButtonHandler() {
    override fun checked() {
        listPlugin.itemClicked(rowPlugin.item)
    }
}