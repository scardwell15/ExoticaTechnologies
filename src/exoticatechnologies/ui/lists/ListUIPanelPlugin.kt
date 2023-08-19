package exoticatechnologies.ui.lists

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin

abstract class ListUIPanelPlugin<T>(var parentPanel: CustomPanelAPI) : InteractiveUIPanelPlugin() {
    abstract val listHeader: String
    open val rowHeight = 64f
    open val rowWidth = 240f
    protected val pad = 3f
    protected val opad = 10f

    private var listeners: MutableList<ListListener<T>> = mutableListOf()
    var panelPluginMap: HashMap<T, BaseUIPanelPlugin> = hashMapOf()

    override var panelWidth: Float = this.getListWidth()
    override var panelHeight: Float = Global.getSettings().screenHeight * 0.66f

    var lastMembers: List<T>? = null
    var outerPanel: CustomPanelAPI? = null
    var outerTooltip: TooltipMakerAPI? = null
    var innerPanel: CustomPanelAPI? = null

    fun getListWidth(): Float {
        return rowWidth
    }

    fun getListHeight(rows: Int): Float {
        return opad + (rowHeight + pad) * rows
    }

    open fun createListHeader(tooltip: TooltipMakerAPI) {
        tooltip.addSectionHeading(listHeader, Alignment.MID, 0f)
    }

    fun layoutPanels(): CustomPanelAPI {
        return layoutPanels(lastMembers!!)
    }

    open fun layoutPanels(members: List<T>): CustomPanelAPI {
        if (outerPanel != null) {
            outerTooltip!!.removeComponent(innerPanel)
            outerPanel!!.removeComponent(outerTooltip)
            parentPanel.removeComponent(outerPanel)
            clearItems()
        }

        val parentPanel = outerPanel ?: parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        outerPanel = parentPanel

        lastMembers = members
        var validMembers = members.filter { shouldMakePanelForItem(it) }
        validMembers = sortMembers(validMembers)

        val parentTooltip = parentPanel.createUIElement(panelWidth, panelHeight, false)
        outerTooltip = parentTooltip

        createListHeader(parentTooltip)

        val heading = parentTooltip.prev

        val holdingPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, null)
        innerPanel = holdingPanel
        val scrollerTooltip: TooltipMakerAPI = holdingPanel.createUIElement(panelWidth, panelHeight, true)
        val scrollingPanel: CustomPanelAPI =
            holdingPanel.createCustomPanel(panelWidth, getListHeight(validMembers.size), null)
        val tooltip: TooltipMakerAPI = scrollingPanel.createUIElement(panelWidth, panelHeight, false)

        var lastItem: CustomPanelAPI? = null

        validMembers
            .map { it to createPanelForItem(tooltip, it) }
            .filter { (_, rowPlugin) -> rowPlugin != null }
            .forEach { (item, rowPlugin) ->
                if (lastItem != null) {
                    rowPlugin?.panel!!.position.belowLeft(lastItem, pad)
                } else {
                    rowPlugin?.panel!!.position.inTL(0f, pad)
                }
                lastItem = rowPlugin.panel!!

                panelPluginMap[item] = rowPlugin
                clickables[rowPlugin.panel!!] = ListItemButtonHandler(rowPlugin, this)
            }

        scrollingPanel.addUIElement(tooltip).inTL(0f, 0f)
        scrollerTooltip.addCustom(scrollingPanel, 0f).position.inTL(0f, 0f)
        holdingPanel.addUIElement(scrollerTooltip).inTL(0f, 0f)
        parentTooltip.addCustom(holdingPanel, 0f).position.belowMid(heading, 0f)
        parentPanel.addUIElement(parentTooltip).inTL(0f, 0f)
        this.parentPanel.addComponent(parentPanel).inTL(0f, 0f)

        return parentPanel
    }

    open fun sortMembers(items: List<T>): List<T> {
        return items
    }

    fun clearItems() {
        panelPluginMap.clear()
        clickables.clear()
    }

    open fun shouldMakePanelForItem(item: T): Boolean {
        return true
    }

    abstract fun createPanelForItem(tooltip: TooltipMakerAPI, item: T): ListItemUIPanelPlugin<T>?

    fun itemClicked(item: T) {
        pickedItem(item)

        listeners.forEach {
            it.pickedItem(item)
        }
    }

    open fun pickedItem(item: T) {
    }

    fun addListener(listener: ListListener<T>) {
        listeners.add(listener)
    }

    fun interface ListListener<T> {
        fun pickedItem(item: T)
    }
}