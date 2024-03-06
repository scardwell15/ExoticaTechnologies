package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.crafting.Recipe
import exoticatechnologies.ui2.tabs.TabListContext
import exoticatechnologies.ui2.tabs.TabListItem
import exoticatechnologies.ui2.tabs.TabListItemContext
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

class RecipeListItem(recipe: Recipe, context: TabListItemContext<Recipe, RecipePanelContext>) :
    TabListItem<Recipe, RecipePanelContext>(recipe, context) {

    override fun decorate(menuPanel: CustomPanelAPI) {
        val itemInfo = menuPanel.createUIElement(innerWidth, panelHeight, false)
        itemInfo.addPara(item.getName(), 0f)
        itemInfo.heightSoFar = panelHeight
        menuPanel.addUIElement(itemInfo).inLMid(innerPadding)

        lunaElement?.onHoverEnter {
            bgColor = Color(255, 255, 255, 166)
        }

        lunaElement?.onHoverExit {
            bgColor = Color(255, 255, 255, 0)
        }
    }


    override fun renderBelow(alphaMult: Float) {
        pos.apply {
            var color = Color.BLACK
            if (currContext.listPanel == currContext.panelContext.recipe) {
                color = Color(255, 255, 255, 100)
            }

            if (bgColor.alpha > 0) {
                color = bgColor
            }

            ColorUtils.glColor(color)

            GL11.glPushMatrix()

            GL11.glTranslatef(0f, 0f, 0f)
            GL11.glRotatef(0f, 0f, 0f, 1f)

            GL11.glEnable(GL11.GL_BLEND)
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

class RecipeItemContext(recipe: Recipe, context: RecipePanelContext) :
    TabListItemContext<Recipe, RecipePanelContext>(recipe, RecipePanel(context)) {
    override val unselectedColor: Color
        get() = Misc.interpolateColor(item.getListItemColor(), Color.BLACK, 0.9f)
    override val highlightedColor: Color
        get() = Misc.interpolateColor(item.getListItemColor(), Color.BLACK, 0.825f)
    override val activeColor: Color
        get() = Misc.interpolateColor(item.getListItemColor(), Color.DARK_GRAY, 0.8f)
    override val activeHighlightedColor: Color
        get() = Misc.interpolateColor(item.getListItemColor(), Color.DARK_GRAY, 0.75f)

    override fun createListItem(listContext: TabListContext<Recipe, RecipePanelContext>): TabListItem<Recipe, RecipePanelContext> {
        return RecipeListItem(item, this)
    }
}