package exoticatechnologies.ui2.tabs

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.ui2.BasePanelContext
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.util.UIUtils
import org.lwjgl.util.vector.Vector2f

open class TabListPanel<I, T : PanelContext>(context: TabListContext<I, T>) :
    RefreshablePanel<TabListContext<I, T>>(context) {
    open var itemWidth = 192f
    open var itemHeight = 72f
    open var itemPadding = 4f

    protected var listItems: MutableList<TabListItem<I, T>> = mutableListOf()
    private var listeners: MutableList<TabListItemListener<I, T>> = mutableListOf()
    private var listContainer: CustomPanelAPI? = null
    var listInnerPanel: CustomPanelAPI? = null
    var listScrollPanel: TooltipMakerAPI? = null
    var activeTabHolder: CustomPanelAPI? = null

    override fun refresh(menuPanel: CustomPanelAPI, context: TabListContext<I, T>) {
        listItems.clear()

        val extraPadding = getListPanelPositionPadding()
        listContainer = createListContainer(menuPanel, context)
        listContainer!!.position.inTL(extraPadding.x + innerPadding, extraPadding.y + innerPadding)

        context.activeItem?.let { activeItem ->
            currContext.tabs.firstOrNull { it.item == activeItem }?.let {
                showTab(it)
            }
        }

        finishedRefresh(menuPanel, context, listItems)
    }

    open fun createListContainer(menuPanel: CustomPanelAPI, context: TabListContext<I, T>): CustomPanelAPI {
        val extraPadding = getListPanelPositionPadding()
        val scrollPanelWidth = itemWidth + 5f

        val listContainer =
            menuPanel.createCustomPanel(itemWidth, innerHeight - extraPadding.y, context.tabsContainerPlugin)
        context.tabsContainerPlugin.panelWidth = itemWidth
        context.tabsContainerPlugin.panelHeight = innerHeight - extraPadding.y

        val innerTooltip = listContainer.createUIElement(scrollPanelWidth, innerHeight - extraPadding.y, true)
        listScrollPanel = innerTooltip

        val innerPanel =
            listContainer.createCustomPanel(itemWidth, (itemHeight + itemPadding) * context.tabs.size + 8f, null)
        listInnerPanel = innerPanel

        context.tabs.forEach {
            val tabButton = it.populateListItem(currContext)
            tabButton.currContext.listPanel = this
            tabButton.panelWidth = itemWidth
            tabButton.panelHeight = itemHeight
            listItems.add(tabButton)
            tabButton.layoutPanel(innerPanel, null)
        }

        innerTooltip.addCustom(innerPanel, 0f).position.inLMid(1f).setYAlignOffset(-1f)
        listContainer.addUIElement(innerTooltip).inMid().setXAlignOffset(-1f)

        menuPanel.addComponent(listContainer)

        return listContainer
    }

    open fun recreateTabHolder(menuPanel: CustomPanelAPI, context: TabListContext<I, T>): CustomPanelAPI {
        activeTabHolder?.let {
            menuPanel.removeComponent(activeTabHolder)
        }

        val extraPadding = getListPanelPositionPadding()
        val scrollPanelWidth = itemWidth + 4f

        activeTabHolder = menuPanel.createCustomPanel(
            innerWidth - extraPadding.x - scrollPanelWidth,
            innerHeight - extraPadding.y,
            context.panelHolderPlugin
        )
        menuPanel.addComponent(activeTabHolder).rightOfMid(listContainer, innerPadding)
        return activeTabHolder!!
    }

    open fun getListPanelPositionPadding(): Vector2f {
        return Vector2f()
    }

    open fun finishedRefresh(menuPanel: CustomPanelAPI, context: TabListContext<I, T>, listItems: List<TabListItem<I, T>>) {
        var lastItem: UIComponentAPI? = null
        listItems
            .map { it.currContext.item }
            .let { currContext.sortItems(it) }
            .mapNotNull { item -> listItems.firstOrNull { it.currContext.item == item } }
            .forEach { listItem ->
                val tooltip: TooltipMakerAPI = listInnerPanel!!.createUIElement(itemWidth, itemHeight, false)
                tooltip.addCustom(listItem.getPanel(), 0f).position.inMid()
                listInnerPanel!!.addUIElement(tooltip).inMid()

                if (lastItem != null) {
                    tooltip.position?.belowMid(lastItem, itemPadding)
                } else {
                    tooltip.position?.inTMid(0f)
                }
                lastItem = tooltip
            }

        var xOffset = 0f
        var yOffset = 0f
        preRefresh { tabListContext, customPanelAPI ->
            xOffset = listScrollPanel!!.externalScroller.xOffset
            yOffset = listScrollPanel!!.externalScroller.yOffset
        }

        postRefresh { tabListContext, customPanelAPI ->
            UIUtils.scrollTo(listScrollPanel!!, xOffset, yOffset)
        }
    }

    fun showTab(tabData: TabListItemContext<I, T>) {
        clearPanel()

        getPanel()?.let {
            recreateTabHolder(it, currContext)
        }

        activeTabHolder?.let { tabHolder ->
            tabData.showPanel(currContext, tabHolder, innerPadding)
            currContext.activeItem = tabData.item
        }
    }

    fun clearPanel() {
        activeTabHolder?.let { tabHolder ->
            currContext.tabs.forEach { it.destroyPanel(this, tabHolder) }
        }

        currContext.activeItem = null
    }

    fun addTab(tabData: TabListItemContext<I, T>) {
        currContext.tabs.apply {
            val index = indexOfFirst { it.item == tabData.item }
            if (index >= 0) {
                removeAt(index)
                currContext.tabs.add(index, tabData)
            } else {
                currContext.tabs.add(tabData)
            }
        }
    }

    open fun createdTabButton(listItem: TabListItem<I, T>) {
    }

    open fun doubleTapDestroysPanel(): Boolean {
        return true
    }

    fun pickedTab(item: TabListItemContext<I, T>) {
        if (doubleTapDestroysPanel() && item.item == currContext.activeItem) {
            clearPanel()
        } else {
            showTab(item)
        }

        callListeners(item)
    }

    fun callListeners(item: TabListItemContext<I, T>) {
        listeners.forEach {
            it.pickedItem(item)
        }
    }

    fun addListener(listener: TabListItemListener<I, T>) {
        listeners.add(listener)
    }

    fun interface TabListItemListener<I, T : PanelContext> {
        fun pickedItem(plugin: TabListItemContext<I, T>)
    }
}

open class TabListContext<I, T : PanelContext> : PanelContext {
    val tabs: MutableList<TabListItemContext<I, T>> = mutableListOf()
    open var activeItem: I? = null
    open var tabsContainerPlugin: RefreshablePanel<PanelContext> = RefreshablePanel(BasePanelContext())
    open var panelHolderPlugin: RefreshablePanel<PanelContext> = RefreshablePanel(BasePanelContext())

    open fun getNewContext(panel: RefreshablePanel<T>): T? {
        return null
    }

    open fun sortItems(items: List<I>): List<I> {
        return items.sortedBy { it.hashCode() }
    }
}