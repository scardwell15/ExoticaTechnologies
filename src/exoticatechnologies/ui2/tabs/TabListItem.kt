package exoticatechnologies.ui2.tabs

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

abstract class TabListItem<I, T : PanelContext>(val item: I, context: TabListItemContext<I, T>) :
    RefreshablePanel<TabListItemContext<I, T>>(context) {
    val panelContext = context.panelContext
    var button: ButtonAPI? = null

    override fun refresh(menuPanel: CustomPanelAPI, context: TabListItemContext<I, T>) {
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
            context.listPanel?.pickedTab(currContext)
        }
        onMouseEnter(button) {
            button.highlight()
        }
        onMouseExit(button) {
            button.unhighlight()
        }

        menuPanel.addUIElement(tooltip).inMid()

        decorate(menuPanel)

        context.listPanel?.createdTabButton(this)
    }

    abstract fun decorate(menuPanel: CustomPanelAPI)


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

                ColorUtils.glColor(color)
            }

            GL11.glPushMatrix()

            GL11.glTranslatef(0f, 0f, 0f)
            GL11.glRotatef(0f, 0f, 0f, 1f)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBegin(GL11.GL_POLYGON)
            GL11.glVertex2f(x, y)
            GL11.glVertex2f(x, y + height)
            GL11.glVertex2f(x + width, y + height)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }
}

abstract class TabListItemContext<I, T : PanelContext>(val item: I, val panel: RefreshablePanel<T>) : PanelContext {
    var listItem: TabListItem<I, T>? = null
    var listPanel: TabListPanel<I, T>? = null
    val panelContext: T
        get() = panel.currContext

    open val activeColor: Color
        get() = Color(225, 225, 225, 200)
    open val highlightedColor: Color
        get() = Color(200, 200, 200, 150)
    open val activeHighlightedColor: Color
        get() = highlightedColor.brighter()
    open val unselectedColor: Color
        get() = Color.BLACK

    open fun populateListItem(listContext: TabListContext<I, T>): TabListItem<I, T> {
        listItem = createListItem(listContext)
        return listItem!!
    }

    protected abstract fun createListItem(listContext: TabListContext<I, T>): TabListItem<I, T>

    open fun isActive(): Boolean {
        listPanel?.let {
            if (it.currContext.activeItem == this.item) {
                return true
            }
        }
        return false
    }

    open fun shownPanelFillsHolder(): Boolean {
        return true
    }

    open fun showPanel(
        tabbedPanelContext: TabListContext<I, T>,
        panelHolder: CustomPanelAPI,
        innerPanelPadding: Float
    ) {
        if (shownPanelFillsHolder()) {
            panel.panelWidth = panelHolder.position.width - innerPanelPadding * 2f
            panel.panelHeight = panelHolder.position.height - innerPanelPadding * 2f
        }

        panel.layoutPanel(panelHolder, tabbedPanelContext.getNewContext(panel))
    }

    open fun destroyPanel(tabbedPanel: TabListPanel<I, T>, panelHolder: CustomPanelAPI) {
        panel.destroyPanel()
    }
}