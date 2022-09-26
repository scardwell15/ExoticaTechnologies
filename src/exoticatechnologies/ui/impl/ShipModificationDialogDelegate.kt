package exoticatechnologies.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.min

class ShipModificationDialogDelegate(var market: MarketAPI) : CustomDialogDelegate {
    var panelHeight: Float = Global.getSettings().screenHeight * 0.65f
    var panelWidth = min(Global.getSettings().screenWidth * 0.85f, 1280f)

    override fun createCustomDialog(panel: CustomPanelAPI?) {
        TODO("Not yet implemented")
    }

    override fun hasCancelButton(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getConfirmText(): String {
        TODO("Not yet implemented")
    }

    override fun getCancelText(): String {
        TODO("Not yet implemented")
    }

    override fun customDialogConfirm() {
        TODO("Not yet implemented")
    }

    override fun customDialogCancel() {
        TODO("Not yet implemented")
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin {
        TODO("Not yet implemented")
    }
}