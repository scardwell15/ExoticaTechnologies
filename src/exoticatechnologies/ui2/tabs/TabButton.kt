package exoticatechnologies.ui2.tabs

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.ui2.PanelContext
import exoticatechnologies.ui2.RefreshablePanel
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_POLYGON
import java.awt.Color
import kotlin.math.max

open class TabButton<T : PanelContext>(context: TabContext<T>) :
    RefreshablePanel<TabContext<T>>(context) {
    override var panelWidth: Float = max(getTabTextWidth(context.tabText) + 4f, 96f)
    override var panelHeight: Float = 28f
    var button: ButtonAPI? = null

    override fun refresh(menuPanel: CustomPanelAPI, context: TabContext<T>) {
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
            context.tabbedPanel?.pickedTab(currContext)
        }
        onMouseEnter(button) {
            button.highlight()
        }
        onMouseExit(button) {
            button.unhighlight()
        }

        menuPanel.addUIElement(tooltip).inMid()
        context.tabbedPanel?.createdTabButton(this)
    }

    override fun render(alphaMult: Float) {
        val cutSize = 8f
        pos.apply {
            button?.let {
                var color = currContext.unselectedColor
                if (currContext.isActive()) {
                    if (it.isHighlighted) {
                        color = currContext.activeHighlightedColor
                    } else {
                        color = currContext.tabColor
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
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glBegin(GL_POLYGON)
            GL11.glVertex2f(x, y)
            GL11.glVertex2f(x, y + height - cutSize)
            GL11.glVertex2f(x + cutSize, y + height)
            GL11.glVertex2f(x + width - cutSize, y + height)
            GL11.glVertex2f(x + width, y + height - cutSize)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
            GL11.glPopMatrix()

            drawableString.text = currContext.tabText
            drawableString.anchor = LazyFont.TextAnchor.CENTER
            drawableString.baseColor = Color.BLACK
            drawableString.draw(x + width / 2f + 1f, y + height / 2f - 1f)
            drawableString.baseColor = currContext.activeHighlightedColor.brighter()
            drawableString.draw(x + width / 2f, y + height / 2f)
        }
    }

    override fun renderBelow(alphaMult: Float) {
    }

    companion object {
        val drawableString: LazyFont.DrawableString = LazyFont.loadFont("graphics/fonts/orbitron20aabold.fnt").let {
            return@let it.createText("")
        }

        fun getTabTextWidth(text: String): Float {
            val oldText = drawableString.text
            drawableString.text = text
            val width = drawableString.width
            drawableString.text = oldText
            return width
        }
    }
}


open class TabContext<T : PanelContext>(
    var panel: RefreshablePanel<T>,
    var tabId: String,
    var tabText: String,
    var tabColor: Color
) : PanelContext {
    var tab: TabButton<T>? = null
    var tabbedPanel: PanelWithTabs<T>? = null

    open val activeHighlightedColor: Color
        get() = tabColor.brighter().brighter()
    open val highlightedColor: Color
        get() = tabColor.brighter()
    open val unselectedColor: Color
        get() = tabColor.darker()

    open fun createNewTabButton(tabbedPanelContext: PanelWithTabsContext<T>): TabButton<T> {
        tab = TabButton(this)
        return tab!!
    }

    open fun shownPanelFillsHolder(): Boolean {
        return true
    }

    open fun showPanel(
        tabbedPanelContext: PanelWithTabsContext<T>,
        panelHolder: CustomPanelAPI,
        innerPanelPadding: Float
    ) {
        if (shownPanelFillsHolder()) {
            panel.panelWidth = panelHolder.position.width - innerPanelPadding * 2f
            panel.panelHeight = panelHolder.position.height - innerPanelPadding * 2f
        }

        panel.layoutPanel(panelHolder, tabbedPanelContext.getNewContext(panel))
    }

    open fun destroyPanel(tabbedPanel: PanelWithTabs<T>, panelHolder: CustomPanelAPI) {
        panel.destroyPanel()
    }

    open fun isActive(): Boolean {
        tabbedPanel?.let {
            if (it.currContext.activeTabId == this.tabId) {
                return true
            }
        }
        return false
    }
}