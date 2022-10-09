package exoticatechnologies.ui

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import exoticatechnologies.util.RenderUtils
import java.awt.Color
import kotlin.math.min

open class BaseUIPanelPlugin: CustomUIPanelPlugin {
    lateinit var pos: PositionAPI
    open var panelWidth: Float = 0f
    open var panelHeight: Float = 0f
    val iconSize : Float
        get() { return min(panelWidth, panelHeight)
        }
    open var bgColor: Color = Color(0, 0, 0, 0)

    override fun positionChanged(position: PositionAPI) {
        pos = position
    }

    override fun renderBelow(alphaMult: Float) {
        if (bgColor.alpha > 0) {
            RenderUtils.pushUIRenderingStack()
            RenderUtils.renderBox(pos.x, pos.y, pos.width, pos.height, bgColor, bgColor.alpha / 255f)
            RenderUtils.popUIRenderingStack()
        }
    }

    override fun render(alphaMult: Float) {
    }

    override fun advance(amount: Float) {
    }

    override fun processInput(events: List<InputEventAPI>) {
    }
}

