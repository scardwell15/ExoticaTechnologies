package exoticatechnologies.ui2.list

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.BasePanel
import exoticatechnologies.ui2.BasePanelContext
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.util.StringUtils
import lunalib.lunaExtensions.addLunaToggleButton
import java.awt.Color

abstract class FilteredListPanel<I>(context: FilteredListContext<I>) : ListPanel<I>(context) {
    val selectedFilters: MutableList<String>
        get() = (currContext as FilteredListContext<I>).selectedFilters
    private val filterCheckboxes: MutableList<ButtonAPI> = mutableListOf()

    override fun createListContainer(menuPanel: CustomPanelAPI, context: ListPanelContext<I>): CustomPanelAPI {
        val scrollPanelWidth = itemWidth + 4f

        val listContainer =
            menuPanel.createCustomPanel(scrollPanelWidth, innerHeight, context.panelHolderPlugin)
        context.panelHolderPlugin.panelWidth = itemWidth
        context.panelHolderPlugin.panelHeight = innerHeight - 24f

        val filterButtonTooltip = listContainer.createUIElement(scrollPanelWidth, 22f, false)
        val filterButtonLocal = filterButtonTooltip.addLunaToggleButton(false, scrollPanelWidth, 20f)
        filterButtonLocal.changeStateText("Confirm", "Filters")
        filterButtonLocal.centerText()
        filterButtonLocal.onClick {
            if (filterButtonLocal.value) {
                createFilterPanel(menuPanel)
            } else {
                closeFilterPanel()
            }
        }
        listContainer.addUIElement(filterButtonTooltip).inBL(-3f, 0f)

        val tabsToShow = context.listItems.filter { shouldAllowItem(it.item) }
        val innerTooltip = listContainer.createUIElement(scrollPanelWidth, innerHeight - 22f, true)
        listScrollPanel = innerTooltip

        val innerPanel =
            listContainer.createCustomPanel(itemWidth, (itemHeight + itemPadding) * tabsToShow.size, null)
        listInnerPanel = innerPanel

        tabsToShow.forEach {
            val tabButton = it.populateListItem(currContext)
            tabButton.currContext.listPanel = this
            tabButton.panelWidth = itemWidth
            tabButton.panelHeight = itemHeight
            listItems.add(tabButton)
            tabButton.layoutPanel(innerPanel, null)
        }

        innerTooltip.addCustom(innerPanel, 0f).position.inLMid(0f)
        listContainer.addUIElement(innerTooltip).inTMid(0f)

        menuPanel.addComponent(listContainer).inTL(innerPadding, innerPadding)

        return listContainer
    }

    override fun finishedRefresh(
        menuPanel: CustomPanelAPI,
        context: ListPanelContext<I>,
        listItems: List<ListItem<I>>
    ) {
        super.finishedRefresh(menuPanel, context, listItems.filter { shouldAllowItem(it.item) })
    }

    open fun createFilterPanel(menuPanel: CustomPanelAPI) {
        val scrollPanelWidth = itemWidth + 4f
        val filterPanelPlugin = BasePanel<PanelContext>(BasePanelContext())
        filterPanelPlugin.renderBackground = true
        filterPanelPlugin.bgColor = Color.BLACK
        filterPanelPlugin.renderBorder = true

        val filterPanelLocal = menuPanel.createCustomPanel(scrollPanelWidth, panelHeight * 0.33f, filterPanelPlugin)
        val filterTooltip = filterPanelLocal.createUIElement(scrollPanelWidth, panelHeight * 0.33f, true)
        getFilters()?.sorted()?.forEach {
            var checkboxText = StringUtils.getString("Filters", it)
            if (checkboxText.contains("Missing string")) {
                checkboxText = it
            }

            filterTooltip.addCheckbox(panelWidth - 6f, 20f, checkboxText, null, ButtonAPI.UICheckboxSize.SMALL, 3f)
                .apply {
                    if (selectedFilters.contains(it)) {
                        isChecked = true
                    }
                    customData = it
                    filterCheckboxes.add(this)
                }
        }
        filterPanelLocal.addUIElement(filterTooltip).inBMid(innerPadding)
        menuPanel.addComponent(filterPanelLocal).inBMid(22f + innerPadding)
    }

    open fun closeFilterPanel() {
        selectedFilters.clear()
        selectedFilters.addAll(filterCheckboxes
            .filter { it.isChecked }
            .map { it.customData.toString() }
        )

        filterCheckboxes.clear()

        refreshPanel()
    }

    abstract fun getFilters(): List<String>?

    abstract fun getFiltersFromItem(item: I): List<String>

    open fun shouldAllowItem(item: I): Boolean {
        if (selectedFilters.isEmpty()) return true
        return getFiltersFromItem(item).any { selectedFilters.contains(it) }
    }
}

open class FilteredListContext<I> : ListPanelContext<I>() {
    val selectedFilters: MutableList<String> = mutableListOf()
}