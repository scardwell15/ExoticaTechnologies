package exoticatechnologies.ui.impl.shop.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import java.awt.Color

abstract class ChipListItemUIPlugin(item: CargoStackAPI,
                           var member: FleetMemberAPI,
                           val listPanel: ListUIPanelPlugin<CargoStackAPI>
                        ) : ListItemUIPanelPlugin<CargoStackAPI>(item) {
    override var bgColor: Color = Color(200, 200, 200, 0)
    var wasHovered: Boolean = false
    override var panelWidth: Float = 64f
    override var panelHeight: Float = 64f

    override fun layoutPanel(tooltip: TooltipMakerAPI): CustomPanelAPI {
        val rowPanel: CustomPanelAPI =
            listPanel.parentPanel.createCustomPanel(panelWidth, panelHeight, this)

        showChip(rowPanel)

        // done, add row to TooltipMakerAPI
        tooltip.addCustom(rowPanel, 0f)
        addedChip(tooltip, rowPanel)

        panel = rowPanel

        return panel!!
    }

    abstract fun showChip(rowPanel: CustomPanelAPI)

    /**
     * Mostly useful for tooltips.
     */
    open fun addedChip(tooltip: TooltipMakerAPI, rowPanel: CustomPanelAPI) {

    }

    override fun processInput(events: List<InputEventAPI>) {
        if (isHovered(events)) {
            if (!wasHovered) {
                wasHovered = true
                setBGColor(alpha = 75)
            }
        } else if (wasHovered) {
            wasHovered = false
            setBGColor(alpha = 0)
        }
    }
}