package exoticatechnologies.ui2.list

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.ui2.BasePanelContext
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import exoticatechnologies.ui2.util.UIUtils

open class ListPanel<I>(context: ListPanelContext<I>) : RefreshablePanel<ListPanelContext<I>>(context) {
    open var itemWidth = 192f
    open var itemHeight = 72f
    open var itemPadding = 4f

    protected var listItems: MutableList<ListItem<I>> = mutableListOf()
    private var listeners: MutableList<ListItemListener<I>> = mutableListOf()
    var listInnerPanel: CustomPanelAPI? = null
    var listScrollPanel: TooltipMakerAPI? = null

    override fun refresh(menuPanel: CustomPanelAPI, context: ListPanelContext<I>) {
        listItems.clear()
        //val titleTooltip = menuPanel.createUIElement(innerWidth, 24f, false)
        //titleTooltip.addSectionHeading(context.listTitle, Alignment.MID, 0f)
        //menuPanel.addUIElement(titleTooltip).inTMid(innerPadding)
        createListContainer(menuPanel, context).position.inTMid(0f) //.belowMid(titleTooltip, innerPadding)

        finishedRefresh(menuPanel, context, listItems)
    }

    open fun createListContainer(menuPanel: CustomPanelAPI, context: ListPanelContext<I>): CustomPanelAPI {
        val scrollPanelWidth = itemWidth + 5f

        val listContainer =
            menuPanel.createCustomPanel(itemWidth, innerHeight, context.panelHolderPlugin)
        context.panelHolderPlugin.panelWidth = itemWidth
        context.panelHolderPlugin.panelHeight = innerHeight - 24f

        val innerTooltip = listContainer.createUIElement(scrollPanelWidth, innerHeight, true)
        listScrollPanel = innerTooltip

        val innerPanel =
            listContainer.createCustomPanel(itemWidth, (itemHeight + itemPadding) * context.listItems.size, null)
        listInnerPanel = innerPanel

        context.listItems.forEach {
            val tabButton = it.populateListItem(currContext)
            tabButton.currContext.listPanel = this
            tabButton.panelWidth = itemWidth
            tabButton.panelHeight = itemHeight
            listItems.add(tabButton)
            tabButton.layoutPanel(innerPanel, null)
        }

        innerTooltip.addCustom(innerPanel, 0f).position.inLMid(1f).setYAlignOffset(-1f)
        listContainer.addUIElement(innerTooltip).inMid().setXAlignOffset(-1f)

        menuPanel.addComponent(listContainer).inTL(innerPadding, innerPadding)
        return listContainer
    }

    fun pickedItem(itemContext: ListItemContext<I>) {
        currContext.selectedItem = itemContext.item
        itemSelected(itemContext)
    }

    open fun itemSelected(itemContext: ListItemContext<I>) {
        callListeners(itemContext)
    }

    open fun finishedRefresh(menuPanel: CustomPanelAPI, context: ListPanelContext<I>, listItems: List<ListItem<I>>) {
            var lastItem: UIComponentAPI? = null
        listItems
            .map { it.currContext.item }
            .let { currContext.sortItems(it) }
            .mapNotNull { item -> listItems.firstOrNull { it.currContext.item == item } }
            .forEach { listItem ->
                val itemPanel = listItem.getPanel()
                val tooltip: TooltipMakerAPI = listInnerPanel!!.createUIElement(itemWidth, itemHeight, false)
                tooltip.addCustom(itemPanel, 0f).position.inMid()
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

    open fun itemCreated(listItem: ListItem<I>) {
    }

    fun callListeners(item: ListItemContext<I>) {
        listeners.forEach {
            it.pickedItem(item)
        }
    }

    fun addListener(listener: ListItemListener<I>) {
        listeners.add(listener)
    }

    fun interface ListItemListener<I> {
        fun pickedItem(plugin: ListItemContext<I>)
    }
}

abstract class ListPanelContext<I> : PanelContext {
    open var listTitle: String = "List"
    var listItems: MutableList<ListItemContext<I>> = mutableListOf()
    var selectedItem: I? = null
    open var panelHolderPlugin: RefreshablePanel<PanelContext> = RefreshablePanel(BasePanelContext())

    open fun sortItems(items: List<I>): List<I> {
        return items.sortedBy { it.hashCode() }
    }
}