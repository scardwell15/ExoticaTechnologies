package exoticatechnologies.refit

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.util.MusicController
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

class ExoticaPanelPlugin(private var parent: UIPanelAPI, private var member: FleetMemberAPI, private var buttonAdder: RefitButtonAdder) : CustomUIPanelPlugin {
    var position: PositionAPI? = null
    var panel: UIPanelAPI? = null
    var closeButtonPanel: UIPanelAPI? = null
    var fadeIn = 0f

    override fun positionChanged(position: PositionAPI?) {
        this.position = position
    }

    override fun renderBelow(alphaMult: Float) {
        if (position == null) return
        var c = Color(0, 0, 0)
        if (CustomExoticaPanel.renderDefaultBackground())
        {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)

            GL11.glDisable(GL11.GL_BLEND)

            GL11.glColor4f(c.red / 255f,
                c.green / 255f,
                c.blue / 255f,
                c.alpha / 255f * (alphaMult * 1f))

            GL11.glRectf(position!!.x,position!!.y , position!!.x + position!!.width, position!!.y + position!!.height)

            //GL11.glEnd()
            GL11.glPopMatrix()
        }

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (alphaMult * fadeIn))

        GL11.glRectf(0f, 0f, Global.getSettings().screenWidth, Global.getSettings().screenHeight)

        //GL11.glEnd()
        GL11.glPopMatrix()
    }

    override fun render(alphaMult: Float) {
        if (position == null) return
        if (!CustomExoticaPanel.renderDefaultBorder()) return

        val x = position!!.x
        val y = position!!.y
        val width = position!!.width
        val height = position!!.height

        var c = Misc.getDarkPlayerColor()
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)

        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (alphaMult * 1f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x, y + height)
        GL11.glVertex2f(x + width, y + height)
        GL11.glVertex2f(x + width, y)
        GL11.glVertex2f(x, y)

        GL11.glEnd()
        GL11.glPopMatrix()
    }

    override fun advance(amount: Float) {
        fadeIn = MathUtils.clamp(fadeIn + 0.05f, 0f, 0.8f)

    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        for (event in events!!)
        {
            if (event.isConsumed) continue
            if (panel == null) continue
            if (event.isKeyboardEvent && event.eventValue == Keyboard.KEY_ESCAPE)
            {
                close()
                event.consume()
                continue
            }
            if (event.isKeyboardEvent)
            {
                event.consume()
            }
            if (event.isMouseMoveEvent || event.isMouseDownEvent)
            {
                event.consume()
            }
        }
    }

    override fun buttonPressed(buttonId: Any?) {

    }

    fun close() {
        MusicController.stopMusic()
        panel?.removeComponent(closeButtonPanel)
        parent.removeComponent(panel)
        buttonAdder.syncVariantIfNeeded()
    }

}