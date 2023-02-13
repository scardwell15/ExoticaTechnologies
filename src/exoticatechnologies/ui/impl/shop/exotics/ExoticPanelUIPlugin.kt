package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.TimedUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.methods.DestroyMethod
import exoticatechnologies.ui.impl.shop.exotics.methods.InstallMethod
import exoticatechnologies.ui.impl.shop.exotics.methods.Method
import exoticatechnologies.ui.impl.shop.exotics.methods.RecoverMethod
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.getMods
import java.awt.Color

class ExoticPanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI,
    var market: MarketAPI
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionPlugin: ExoticDescriptionUIPlugin? = null
    private var methodsPlugin: ExoticMethodsUIPlugin? = null
    private var resourcesPlugin: ExoticResourcesUIPlugin? = null

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        descriptionPlugin = ExoticDescriptionUIPlugin(panel, exotic, member)
        descriptionPlugin!!.panelWidth = panelWidth / 2
        descriptionPlugin!!.panelHeight = panelHeight
        descriptionPlugin!!.layoutPanels().position.inTL(0f, 0f)

        val methods = getMethods()

        resourcesPlugin = ExoticResourcesUIPlugin(panel, exotic, member, market, methods)
        resourcesPlugin!!.panelWidth = panelWidth / 2
        resourcesPlugin!!.panelHeight = panelHeight / 2
        resourcesPlugin!!.layoutPanels().position.inTR(0f, 0f)

        methodsPlugin = ExoticMethodsUIPlugin(panel, exotic, member, market, methods)
        methodsPlugin!!.panelWidth = panelWidth / 2
        methodsPlugin!!.panelHeight = panelHeight / 2
        methodsPlugin!!.layoutPanels().position.inBR(0f, 0f)
        methodsPlugin!!.addListener(MethodListener(this))

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    private fun getMethods(): List<Method> {
        return mutableListOf(
            InstallMethod(),
            RecoverMethod(),
            DestroyMethod()
        )
    }

    fun checkedMethod(method: Method): Boolean {
        applyMethod(exotic, method)
        return false
    }

    fun highlightedMethod(method: Method?): Boolean {
        resourcesPlugin!!.redisplayResourceCosts(method)
        return false
    }

    fun applyMethod(exotic: Exotic, method: Method) {
        val mods = member.getMods()
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        method.apply(member, mods, exotic, market)

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)

        resourcesPlugin!!.redisplayResourceCosts(method)
        methodsPlugin!!.showTooltip()
    }

    private class MethodListener(val mainPlugin: ExoticPanelUIPlugin) : ExoticMethodsUIPlugin.Listener() {
        override fun checked(method: Method): Boolean {
            return mainPlugin.checkedMethod(method)
        }

        override fun highlighted(method: Method): Boolean {
            return mainPlugin.highlightedMethod(method)
        }

        override fun unhighlighted(method: Method): Boolean {
            return mainPlugin.highlightedMethod(null)
        }
    }

    private class AppliedUIListener(val mainPlugin: ExoticPanelUIPlugin, val tooltip: TooltipMakerAPI) :
        TimedUIPlugin.Listener {
        override fun end() {
            mainPlugin.mainPanel!!.removeComponent(tooltip)
            mainPlugin.resourcesPlugin!!.redisplayResourceCosts(null)
            mainPlugin.methodsPlugin!!.showTooltip()
        }

        override fun render(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float) {

        }

        override fun renderBelow(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float) {
            RenderUtils.pushUIRenderingStack()
            val panelX = pos.x
            val panelY = pos.y
            val panelW = pos.width
            val panelH = pos.height
            RenderUtils.renderBox(
                panelX,
                panelY,
                panelW,
                panelH,
                Color.yellow,
                alphaMult * (endLife - currLife) / endLife
            )
            RenderUtils.popUIRenderingStack()
        }
    }
}