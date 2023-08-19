package exoticatechnologies.ui.lists.filtered

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.lists.ListItemButtonHandler
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import lunalib.lunaExtensions.addLunaToggleButton
import lunalib.lunaUI.elements.LunaToggleButton

abstract class FilteredListPanelPlugin<T>(parentPanel: CustomPanelAPI) : ListUIPanelPlugin<T>(parentPanel) {
    var filterButton: LunaToggleButton? = null
    var filterPanel: CustomPanelAPI? = null
    var filterCheckboxes: MutableList<ButtonAPI> = mutableListOf()
    var selectedFilters: MutableList<String> = mutableListOf()

    override fun layoutPanels(members: List<T>): CustomPanelAPI {
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

        val scrollingPanelHeight = panelHeight - 22f
        val scrollerTooltip: TooltipMakerAPI = holdingPanel.createUIElement(panelWidth, scrollingPanelHeight, true)
        val scrollingPanel: CustomPanelAPI =
            holdingPanel.createCustomPanel(panelWidth, getListHeight(validMembers.size), null)
        val tooltip: TooltipMakerAPI = scrollingPanel.createUIElement(panelWidth, scrollingPanelHeight, false)

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


        val filterButtonTooltip = holdingPanel.createUIElement(panelWidth, 20f, false)
        val filterButtonLocal = filterButtonTooltip.addLunaToggleButton(false, panelWidth, 20f)
        filterButtonLocal.changeStateText("Confirm", "Filters")
        filterButtonLocal.centerText()

        filterButtonLocal.onClick {
            if (filterButtonLocal.value) {
                createFilterPanel()
            } else {
                closeFilterPanel()
            }
        }

        holdingPanel.addUIElement(filterButtonTooltip).inBL(-3f, 0f)

        parentTooltip.addCustom(holdingPanel, 0f).position.belowMid(heading, 0f)

        parentPanel.addUIElement(parentTooltip).inTL(0f, 0f)
        this.parentPanel.addComponent(parentPanel).inTL(0f, 0f)

        return parentPanel
    }

    abstract fun getFilters(): List<String>?

    fun createFilterPanel() {
        val filterPanelPlugin = BaseUIPanelPlugin()
        filterPanelPlugin.renderBackground = true

        val filterPanelLocal = outerPanel!!.createCustomPanel(panelWidth, panelHeight * 0.33f, filterPanelPlugin)
        filterPanel = filterPanelLocal

        val filterTooltip = filterPanelLocal.createUIElement(panelWidth, panelHeight * 0.33f, true)
        getFilters()?.forEach {
            var checkboxText = StringUtils.getString("Filters", it)
            if (checkboxText.contains("Missing string")) {
                checkboxText = it
            }

            filterTooltip.addCheckbox(panelWidth - 6f, 20f, checkboxText, null, ButtonAPI.UICheckboxSize.SMALL, 3f).apply {
                if (selectedFilters.contains(it)) {
                    isChecked = true
                }
                customData = it
                filterCheckboxes.add(this)
            }
        }
        filterPanelLocal.addUIElement(filterTooltip).inBMid(2f)

        outerPanel!!.addComponent(filterPanelLocal).inBMid(2f)
    }

    fun closeFilterPanel() {
        selectedFilters.clear()
        selectedFilters.addAll(filterCheckboxes
            .filter { it.isChecked }
            .map { it.customData.toString() }
        )

        filterCheckboxes.clear()
        outerPanel!!.removeComponent(filterPanel)

        layoutPanels()
    }

    abstract fun getFiltersFromItem(item: T): List<String>

    override fun shouldMakePanelForItem(item: T): Boolean {
        if (selectedFilters.isEmpty()) return true
        return getFiltersFromItem(item).any { selectedFilters.contains(it) }
    }
}