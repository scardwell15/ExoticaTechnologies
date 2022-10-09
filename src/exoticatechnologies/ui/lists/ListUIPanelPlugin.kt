package exoticatechnologies.ui.lists

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.util.StringUtils

abstract class ListUIPanelPlugin<T>(var parentPanel: CustomPanelAPI) : InteractiveUIPanelPlugin() {
    open val listHeader = StringUtils.getTranslation("FleetScanner", "NotableShipsHeader").toString()
    open val rowSize = 64f
    private val pad = 3f
    private val opad = 10f
    private val textWidth = 240f

    private var listeners: MutableList<ListListener<T>> = mutableListOf()

    override var panelWidth: Float = this.getListWidth()
    override var panelHeight: Float = Global.getSettings().screenHeight * 0.66f

    var panelPluginMap: HashMap<T, BaseUIPanelPlugin> = hashMapOf()

    private fun getListWidth() : Float {
        return textWidth + rowSize
    }

    private fun getListHeight(rows: Int) : Float {
        return opad + (rowSize + pad) * rows
    }

    fun layoutPanels(members: MutableList<T>): CustomPanelAPI {
        val outerPanel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        val outerTooltip = outerPanel.createUIElement(panelWidth, panelHeight, false)

        outerTooltip.addSectionHeading(listHeader, Alignment.MID, 0f)
        val heading = outerTooltip.prev

        val innerPanel = outerPanel.createCustomPanel(panelWidth, panelHeight, null)
        val scrollerTooltip: TooltipMakerAPI = innerPanel.createUIElement(panelWidth, panelHeight, true)
        val scrollingPanel: CustomPanelAPI = innerPanel.createCustomPanel(panelWidth, getListHeight(members.size), null)
        val tooltip: TooltipMakerAPI = scrollingPanel.createUIElement(panelWidth, panelHeight, false)

        var lastItem: CustomPanelAPI? = null
        members.forEach {
            val rowPlugin = createPanelForItem(tooltip, it)

            if (lastItem != null) {
                rowPlugin.panel!!.position.belowLeft(lastItem, pad)
            } else {
                rowPlugin.panel!!.position.inTL(0f, pad)
            }
            lastItem = rowPlugin.panel!!

            panelPluginMap[it] = rowPlugin
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

    abstract fun createPanelForItem(tooltip: TooltipMakerAPI, item: T): ListItemUIPanelPlugin<T>

    fun pickedItem(item: T) {
        listeners.forEach {
            it.pickedItem(item)
        }
    }

    fun addListener(listener: ListListener<T>) {
        listeners.add(listener)
    }

    fun interface ListListener<T> {
        fun pickedItem(item: T)
    }
}