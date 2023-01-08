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
    private val pad = 3f
    private val opad = 10f

    private var listeners: MutableList<ListListener<T>> = mutableListOf()

    override var panelWidth: Float = this.getListWidth()
    override var panelHeight: Float = Global.getSettings().screenHeight * 0.66f

    var panelPluginMap: HashMap<T, BaseUIPanelPlugin> = hashMapOf()

    private fun getListWidth(): Float {
        return rowWidth
    }

    private fun getListHeight(rows: Int): Float {
        return opad + (rowHeight + pad) * rows
    }

    open fun createListHeader(tooltip: TooltipMakerAPI) {
        tooltip.addSectionHeading(listHeader, Alignment.MID, 0f)
    }

    fun layoutPanels(members: List<T>): CustomPanelAPI {
        val validMembers = members.filter { shouldMakePanelForItem(it) }

        val outerPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val outerTooltip = outerPanel.createUIElement(panelWidth, panelHeight, false)

        createListHeader(outerTooltip)

        val heading = outerTooltip.prev

        val innerPanel = outerPanel.createCustomPanel(panelWidth, panelHeight, null)
        val scrollerTooltip: TooltipMakerAPI = innerPanel.createUIElement(panelWidth, panelHeight, true)
        val scrollingPanel: CustomPanelAPI =
            innerPanel.createCustomPanel(panelWidth, getListHeight(validMembers.size), null)
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
        innerPanel.addUIElement(scrollerTooltip).inTL(0f, 0f)
        outerTooltip.addCustom(innerPanel, 0f).position.belowMid(heading, 0f)
        outerPanel.addUIElement(outerTooltip).inTL(0f, 0f)
        parentPanel.addComponent(outerPanel).inTL(0f, 0f)

        return outerPanel
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