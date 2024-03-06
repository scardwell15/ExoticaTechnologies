package exoticatechnologies.ui2.list

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_POLYGON
import java.awt.Color

open class ListItem<I>(context: ListItemContext<I>) :
    RefreshablePanel<ListItemContext<I>>(context) {
    val item: I
        get() = currContext.item

    var button: ButtonAPI? = null
    var disabled: Boolean = false

    override fun refresh(menuPanel: CustomPanelAPI, context: ListItemContext<I>) {
        renderBorder = true
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)

        val button = tooltip.addAreaCheckbox(
            "",
            null,
            Color(255, 255, 255, 1),
            Color(255, 255, 255, 1),
            Color(255, 255, 255, 1),
            panelWidth,
            panelHeight,
            2f
        )
        this.button = button
        button.position.inMid()
        onClick(button) {
            if (disabled) return@onClick
            this.currContext.listPanel?.pickedItem(currContext)
        }
        onMouseEnter(button) {
            if (disabled) return@onMouseEnter
            button.highlight()
        }
        onMouseExit(button) {
            if (disabled) return@onMouseExit
            button.unhighlight()
        }

        menuPanel.addUIElement(tooltip).inMid()

        decorate(menuPanel)

        context.listPanel?.itemCreated(this)
    }

    open fun decorate(menuPanel: CustomPanelAPI) {

    }

    override fun renderBelow(alphaMult: Float) {
        pos.apply {
            button?.let {
                var color = currContext.unselectedColor
                if (currContext.isActive()) {
                    if (it.isHighlighted) {
                        color = currContext.activeHighlightedColor
                    } else {
                        color = currContext.activeColor
                    }
                } else if (it.isHighlighted) {
                    color = currContext.highlightedColor
                }

                if (bgColor.alpha > 0 && renderBackground) {
                    color = bgColor
                }

                ColorUtils.glColor(color)
            }

            GL11.glPushMatrix()
            GL11.glTranslatef(0f, 0f, 0f)
            GL11.glRotatef(0f, 0f, 0f, 1f)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glBegin(GL_POLYGON)
            GL11.glVertex2f(x, y)
            GL11.glVertex2f(x, y + height)
            GL11.glVertex2f(x + width, y + height)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }

    companion object {
        val drawableString: LazyFont.DrawableString = LazyFont.loadFont("graphics/fonts/orbitron20aabold.fnt").let {
            return@let it.createText("")
        }

        private fun getTextWidth(text: String): Float {
            val oldText = drawableString.text
            drawableString.text = text
            val width = drawableString.width
            drawableString.text = oldText
            return width
        }
    }
}

open class ListItemContext<I>(
    var item: I
) : PanelContext {
    var itemPanel: ListItem<I>? = null
    var listPanel: ListPanel<I>? = null

    open val activeColor: Color
        get() = Color(255, 255, 255, 200)
    open val highlightedColor: Color
        get() = Color(255, 255, 255, 100)
    open val activeHighlightedColor: Color
        get() = highlightedColor.brighter()
    open val unselectedColor: Color
        get() = Color.BLACK

    fun populateListItem(listContext: ListPanelContext<I>): ListItem<I> {
        itemPanel = createListItem(listContext)
        return itemPanel!!
    }

    protected open fun createListItem(listContext: ListPanelContext<I>): ListItem<I> {
        return ListItem(this)
    }

    open fun isActive(): Boolean {
        listPanel?.let {
            if (it.currContext.selectedItem == this.item) {
                return true
            }
        }
        return false
    }
}