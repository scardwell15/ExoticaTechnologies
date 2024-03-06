package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.crafting.ingredients.spec.IngredientSpec
import exoticatechnologies.ui2.RefreshablePanel
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

class RecipeIngredientPanel(context: RecipeIngredientPanelContext) :
    RefreshablePanel<RecipeIngredientPanelContext>(context) {

    private val ingredientSpec: IngredientSpec<out Ingredient>
        get() = currContext.ingredientSpec

    override var renderBorder: Boolean = true
    override var outlineColor: Color = Color.RED
    private var iconTooltip: TooltipMakerAPI? = null

    override fun refresh(menuPanel: CustomPanelAPI, context: RecipeIngredientPanelContext) {
        if (context.ingredientSpec.hasRequiredAmount(context.selectedIngredients)) {
            outlineColor = Misc.getBrightPlayerColor()
        } else {
            outlineColor = Color(200, 80, 80)
        }

        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)
        iconTooltip = tooltip
        ingredientSpec.decorateRecipeIngredient(tooltip, currContext.selectedIngredients, currContext.selectorOpen)
        tooltip.heightSoFar = panelHeight
        menuPanel.addUIElement(tooltip).inMid()

        lunaElement?.onHoverEnter {
            bgColor = Color(255, 255, 255, 166)
        }

        lunaElement?.onHoverExit {
            bgColor = Color(255, 255, 255, 0)
        }

        currContext.refreshed(this)
    }

    override fun renderBelow(alphaMult: Float) {
        pos.apply {
            var color = Color.BLACK
            if (currContext.recipePanelContext.selectedSpecIndex == currContext.index) {
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

open class RecipeIngredientPanelContext(
    val recipePanel: RecipePanel,
    val recipePanelContext: RecipePanelContext,
    var ingredientSpec: IngredientSpec<out Ingredient>,
    val index: Int
) : CraftingPanelContext(recipePanelContext.fleet, recipePanelContext.market) {
    val selectedIngredients: MutableList<Ingredient> = mutableListOf()
    var selectorOpen = false

    fun refreshed(ingredientPanel: RecipeIngredientPanel) {
        recipePanel.recipeSelectorRefreshed(ingredientPanel)
    }

    fun setSelectedIngredients(ingredients: List<Ingredient>) {
        selectedIngredients.clear()
        selectedIngredients.addAll(ingredients)
    }

    fun createIngredients(): List<Ingredient> {
        return ingredientSpec.createIngredients(fleet, market)
            .filter { recipePanelContext.recipe.canUseIngredient(index, ingredientSpec, it) }
            .filter { recipePanelContext.hasIngredientForSpec(index, ingredientSpec, it) }
    }

    fun isIngredientSelected(ingredient: Ingredient): Boolean {
        return selectedIngredients.any { it == ingredient }
    }
}

