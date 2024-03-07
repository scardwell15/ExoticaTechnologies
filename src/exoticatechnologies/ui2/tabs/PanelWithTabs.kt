package exoticatechnologies.ui2.tabs

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.BasePanelContext
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import org.lwjgl.util.vector.Vector2f

open class PanelWithTabs<T: PanelContext>(context: PanelWithTabsContext<T>) :
    RefreshablePanel<PanelWithTabsContext<T>>(context) {
    open val tabHeight = 28f
    open val tabPadding = 2f

    private var listeners: MutableList<TabButtonListener<T>> = mutableListOf()
    private var tabsContainer: CustomPanelAPI? = null
    private var activeTabHolder: CustomPanelAPI? = null

    override fun refresh(menuPanel: CustomPanelAPI, context: PanelWithTabsContext<T>) {
        val innerTooltip = menuPanel.createUIElement(innerWidth, innerHeight, false)
        val extraPadding = getTabPanelPositionPadding()

        val tabsSize = getTabPanelSizeOverride() ?: Vector2f(context.tabs.sumOf { TabButton.getTabTextWidth(it.tabText).toDouble() + innerPadding }.toFloat() - extraPadding.x, tabHeight)
        tabsContainer = menuPanel.createCustomPanel(tabsSize.x, tabsSize.y, context.tabsContainerPlugin)
        context.tabsContainerPlugin.panelWidth = innerWidth - extraPadding.x
        context.tabsContainerPlugin.panelHeight = tabHeight
        tabsContainer?.let { tabsContainer ->
            var lastButtonPanel: CustomPanelAPI? = null
            context.tabs.forEach {
                val tabButton = it.createNewTabButton(currContext)
                tabButton.currContext.tabbedPanel = this
                val buttonPanel = tabButton.layoutPanel(tabsContainer, null)

                positionTabButton(lastButtonPanel, buttonPanel)

                lastButtonPanel = buttonPanel
            }
        }

        innerTooltip.addCustom(tabsContainer, 0f).position.inTL(extraPadding.x + innerPadding, extraPadding.y + innerPadding)

        var panelHolderWidth = innerWidth - extraPadding.x
        var panelHolderHeight = innerHeight - tabHeight - innerPadding - extraPadding.y
        getPanelHolderSizeOverride()?.let {
            panelHolderWidth = it.x
            panelHolderHeight = it.y
        }

        activeTabHolder = menuPanel.createCustomPanel(panelHolderWidth, panelHolderHeight, context.panelHolderPlugin)
        innerTooltip.addCustom(activeTabHolder, 0f)
        positionPanelHolder(activeTabHolder!!, tabsContainer!!)

        menuPanel.addUIElement(innerTooltip).inTL(0f, 0f)

        context.activeTabId?.let { tabId ->
            currContext.tabs.firstOrNull { it.tabId == tabId }?.let {
                showTab(it)
            }
        }

        finishedRefresh(menuPanel, context)
    }

    protected open fun positionTabButton(
        lastButtonPanel: CustomPanelAPI?,
        buttonPanel: CustomPanelAPI
    ) {
        if (lastButtonPanel != null) {
            buttonPanel.position.rightOfMid(lastButtonPanel, tabPadding)
        } else {
            buttonPanel.position.inTL(tabPadding, 0f)
        }
    }

    open fun getTabPanelSizeOverride(): Vector2f? {
        return null
    }

    open fun getTabPanelPositionPadding(): Vector2f {
        return Vector2f()
    }

    open fun positionPanelHolder(activeTabHolder: CustomPanelAPI, tabsContainer: CustomPanelAPI) {
        activeTabHolder.position.inTL(innerPadding, tabHeight + innerPadding)
    }

    open fun getPanelHolderSizeOverride(): Vector2f? {
        return null
    }

    open fun finishedRefresh(menuPanel: CustomPanelAPI, context: PanelWithTabsContext<T>) {

    }

    fun showTab(tabData: TabContext<T>) {
        clearPanel()
        activeTabHolder?.let { tabHolder ->
            tabData.showPanel(currContext, tabHolder, innerPadding)
            currContext.activeTabId = tabData.tabId
        }
    }

    fun clearPanel() {
        activeTabHolder?.let { tabHolder ->
            currContext.tabs.forEach { it.destroyPanel(this, tabHolder) }
        }
        currContext.activeTabId = null
    }

    fun addTab(tabData: TabContext<T>) {
        currContext.tabs.apply {
            val index = indexOfFirst { it.tabId == tabData.tabId }
            if (index >= 0) {
                removeAt(index)
                currContext.tabs.add(index, tabData)
            } else {
                currContext.tabs.add(tabData)
            }
        }
    }

    open fun createdTabButton(tabButton: TabButton<T>) {

    }

    open fun doubleTapDestroysPanel(): Boolean {
        return true
    }

    fun pickedTab(item: TabContext<T>) {
        if (doubleTapDestroysPanel() && item.tabId == currContext.activeTabId) {
            clearPanel()
        } else {
            showTab(item)
        }

        callListeners(item)
    }

    fun callListeners(item: TabContext<T>) {
        listeners.forEach {
            it.pickedItem(item)
        }
    }

    fun addListener(listener: TabButtonListener<T>) {
        listeners.add(listener)
    }

    fun interface TabButtonListener<T : PanelContext> {
        fun pickedItem(plugin: TabContext<T>)
    }
}

open class PanelWithTabsContext<T: PanelContext> : PanelContext {
    val tabs: MutableList<TabContext<T>> = mutableListOf()
    var activeTabId: String? = null
    open var tabsContainerPlugin: RefreshablePanel<PanelContext> = RefreshablePanel(BasePanelContext())
    open var panelHolderPlugin: RefreshablePanel<PanelContext> = RefreshablePanel(BasePanelContext())

    open fun getNewContext(panel: RefreshablePanel<T>): T? {
        return null
    }
}