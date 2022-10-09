package exoticatechnologies.ui.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemKeys
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.ui.BaseUIPanelPlugin
import exoticatechnologies.ui.impl.ships.ShipListUIPanelPlugin
import exoticatechnologies.ui.impl.shop.ShipModUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color
import kotlin.math.min

fun Color.modify(red: Int? = null, green: Int? = null, blue: Int? = null, alpha: Int? = null) =
    Color(red ?: this.red, green ?: this.green, blue ?: this.blue, alpha ?: this.alpha)

class ShipModificationDialogDelegate(var dialog: InteractionDialogAPI, var market: MarketAPI) : CustomDialogDelegate {
    var panelHeight: Float = Global.getSettings().screenHeight * 0.65f
    var panelWidth = min(Global.getSettings().screenWidth * 0.85f, 1280f)

    var members: MutableList<FleetMemberAPI> = mutableListOf()
    var plugin: CustomUIPanelPlugin = BaseUIPanelPlugin()

    override fun createCustomDialog(panel: CustomPanelAPI) {
        members = Global.getSector().playerFleet.membersWithFightersCopy
            .filterNot {it.isFighterWing}
            .toMutableList()

        val listPlugin = ShipListUIPanelPlugin(panel)
        listPlugin.panelHeight = panelHeight
        val shipListTooltip = listPlugin.layoutPanels(members)

        val modPlugin = ShipModUIPanelPlugin(dialog, panel, panelWidth - listPlugin.panelWidth, panelHeight)
        val shipModTooltip = modPlugin.layoutPanels()

        listPlugin.addListener { member ->
            run {
                listPlugin.panelPluginMap.values.forEach { it.bgColor.modify(alpha = 0) }
                listPlugin.panelPluginMap[member]?.bgColor?.modify(alpha = 100)
                modPlugin.showPanel(member)
            }
        }
    }

    override fun hasCancelButton(): Boolean {
        return false
    }

    override fun getConfirmText(): String {
        return StringUtils.getString("Options","confirm")
    }

    override fun getCancelText(): String {
        return StringUtils.getString("Options","cancel")
    }

    override fun customDialogConfirm() {
        ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), dialog.plugin.memoryMap)
        dialog.plugin.memoryMap[MemKeys.LOCAL]!!["\$option", "ETDialogBack"] = 0f
        FireAll.fire(null, dialog, dialog.plugin.memoryMap, "DialogOptionSelected")
    }

    override fun customDialogCancel() {
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin {
        return plugin
    }
}