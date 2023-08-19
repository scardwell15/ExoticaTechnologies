package exoticatechnologies.ui

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import exoticatechnologies.util.RenderUtils
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lwjgl.opengl.GL11

open class SpritePanelPlugin(val sprite: SpriteAPI) : CustomUIPanelPlugin {
    var pos: PositionAPI? = null
    var panel: CustomPanelAPI? = null
    var saturation: Float = 1f

    override fun positionChanged(position: PositionAPI?) {
        pos = position
    }

    override fun renderBelow(alphaMult: Float) {
    }

    override fun render(alphaMult: Float) {
        pos?.let {
            val x = pos!!.x
            val y = pos!!.y
            val w = pos!!.width
            val h = pos!!.height
            val color = sprite.color

            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.textureId)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glBegin(GL11.GL_QUADS)

            ColorUtils.glColor(
                color
            )
            //Sets corner 1, or the first left corner
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(x - 1.0f, y - 1.0f)

            //Sets corner 2, or the first right corner
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(x + w + 1.0f, y - 1.0f)

            //Sets corner 3, or the second right corner
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(x + w + 1.0f, y + h + 1.0f)

            //Sets corner 4, or the second left corner
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(x - 1.0f, y + h + 1.0f)
            GL11.glEnd()
        }
    }

    override fun advance(amount: Float) {
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
    }

    override fun buttonPressed(buttonId: Any?) {
    }
}