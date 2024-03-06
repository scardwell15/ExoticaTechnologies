package exoticatechnologies.ui2

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import java.awt.Color

open class BasePanel<T : PanelContext>(open var currContext: T) : CustomUIPanelPlugin {
    lateinit var pos: PositionAPI
    open var panelWidth: Float = 0f
    open var panelHeight: Float = 0f
    open var renderBackground: Boolean = false
    open var renderBorder: Boolean = false
    open var bgColor: Color = Color(0, 0, 0, 0)
    open var outlineColor: Color = Misc.getDarkPlayerColor()

    override fun positionChanged(position: PositionAPI) {
        pos = position
    }

    override fun renderBelow(alphaMult: Float) {
        if (renderBackground && bgColor.alpha > 0) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glColor4f(
                bgColor.red / 255f,
                bgColor.green / 255f,
                bgColor.blue / 255f,
                bgColor.alpha / 255f * (alphaMult * 1f)
            )
            GL11.glRectf(pos.x, pos.y, pos.x + pos.width, pos.y + pos.height)

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
        }
    }

    override fun render(alphaMult: Float) {
        if (renderBorder) {
            val x = pos.x
            val y = pos.y
            val width = pos.width
            val height = pos.height

            val c = outlineColor
            GL11.glPushMatrix()

            GL11.glTranslatef(0f, 0f, 0f)
            GL11.glRotatef(0f, 0f, 0f, 1f)

            GL11.glDisable(GL11.GL_TEXTURE_2D)

            if (c.alpha < 255 || alphaMult < 1f) {
                GL11.glEnable(GL11.GL_BLEND)
            } else {
                GL11.glDisable(GL11.GL_BLEND)
            }

            GL11.glColor4f(
                c.red / 255f,
                c.green / 255f,
                c.blue / 255f,
                c.alpha / 255f * (alphaMult * 1f)
            )

            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            GL11.glVertex2f(x, y)
            GL11.glVertex2f(x, y + height)
            GL11.glVertex2f(x + width, y + height)
            GL11.glVertex2f(x + width, y)
            GL11.glVertex2f(x, y)

            GL11.glDisable(GL11.GL_BLEND)

            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }

    override fun advance(amount: Float) {
    }

    override fun processInput(events: List<InputEventAPI>) {
    }

    override fun buttonPressed(buttonId: Any?) {
    }
}

class BasePanelContext : PanelContext {

}

interface PanelContext {

}