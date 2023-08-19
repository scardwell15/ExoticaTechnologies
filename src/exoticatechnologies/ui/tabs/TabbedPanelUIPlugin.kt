package exoticatechnologies.ui.tabs

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin

open class TabbedPanelUIPlugin(var parentPanel: CustomPanelAPI) : InteractiveUIPanelPlugin() {
    open val tabHeight = 30f
    private val pad = 3f
    private val opad = 10f

    private var listeners: MutableList<TabButtonListener> = mutableListOf()
    private var innerPanel: CustomPanelAPI? = null
    private var activeTabElement: TooltipMakerAPI? = null
    private var activeTabPlugin: TabPanelUIPlugin? = null

    fun layoutPanels(tooltip: TooltipMakerAPI, members: List<TabPanelUIPlugin>, tabHolderPlugin: BaseUIPanelPlugin? = null, holderPanelPlugin: BaseUIPanelPlugin? = null): CustomPanelAPI {
        val myPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val innerTooltip = myPanel.createUIElement(panelWidth, panelHeight, false)
        val tabHolderPanel = myPanel.createCustomPanel(panelWidth, tabHeight, tabHolderPlugin)
        innerPanel = myPanel.createCustomPanel(panelWidth, panelHeight - tabHeight, holderPanelPlugin)

        var lastItem: TooltipMakerAPI? = null
        members.forEach {
            val rowElement = createTabButtonForItem(tabHolderPanel, it)

            if (lastItem != null) {
                rowElement.position.rightOfTop(lastItem, pad)
            } else {
                rowElement.position.inTL(0f, pad)
            }
            lastItem = rowElement

            if (it.getTabButtonUIPlugin().isButtonApi) {
                buttons[it.getTabButtonUIPlugin().button!!] = it.getNewButtonHandler(this)
            } else {
                clickables[rowElement] = it.getNewButtonHandler(this)
            }
        }

        innerTooltip.addCustom(tabHolderPanel, 0f).position.inTL(0f, 0f)
        innerTooltip.addCustom(innerPanel, 0f)
        myPanel.addUIElement(innerTooltip).inTL(0f, 0f)
        pos = tooltip.addCustom(myPanel, 0f).position

        return myPanel
    }

    fun pickedPanel(item: TabPanelUIPlugin?) {
        innerPanel!!.removeComponent(activeTabElement)

        if (activeTabPlugin != null) {
            var shortcut = false
            activeTabPlugin!!.deactivated(this)

            if (item == activeTabPlugin) {
                shortcut = true
            }

            if (shortcut) {
                activeTabPlugin = null
                activeTabElement = null
                return
            }
        }

        if (item != null) {
            item.panelWidth = innerPanel!!.position.width - pad * 2
            item.panelHeight = innerPanel!!.position.height - pad * 2
            item.activated(this)
            activeTabPlugin = item
            activeTabElement = showPanel(item)
        } else {
            activeTabElement = showNothing()
        }
    }

    private fun showNothing(): TooltipMakerAPI? {
        val tooltip = innerPanel!!.createUIElement(panelWidth, panelHeight, false)

        innerPanel!!.addUIElement(tooltip)?.inTL(0f, 0f)

        return tooltip
    }

    private fun showPanel(item: TabPanelUIPlugin): TooltipMakerAPI? {
        return item.layoutPanel(innerPanel!!, this)
    }

    protected open fun createTabButtonForItem(tooltip: CustomPanelAPI, plugin: TabPanelUIPlugin): TooltipMakerAPI {
        return plugin.getTabButtonUIPlugin().createTabButton(tooltip, this)
    }

    fun pickedTab(item: TabPanelUIPlugin) {
        callListeners(item)
        pickedPanel(item)
    }

    fun callListeners(item: TabPanelUIPlugin) {
        listeners.forEach {
            it.pickedItem(item)
        }
    }

    fun addListener(listener: TabButtonListener) {
        listeners.add(listener)
    }

    fun interface TabButtonListener {
        fun pickedItem(plugin: TabPanelUIPlugin)
    }
}