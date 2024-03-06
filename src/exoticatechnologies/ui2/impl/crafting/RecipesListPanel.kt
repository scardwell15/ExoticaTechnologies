package exoticatechnologies.ui2.impl.crafting

import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.crafting.Recipe
import exoticatechnologies.crafting.RecipeManager
import exoticatechnologies.ui2.tabs.FilteredTabListContext
import exoticatechnologies.ui2.tabs.FilteredTabListPanel
import exoticatechnologies.ui2.tabs.TabListContext
import exoticatechnologies.ui2.tabs.TabListItem

class RecipesListPanel(context: RecipesListContext) :
    FilteredTabListPanel<Recipe, RecipePanelContext>(context) {

    override fun finishedRefresh(
        menuPanel: CustomPanelAPI,
        context: TabListContext<Recipe, RecipePanelContext>,
        listItems: List<TabListItem<Recipe, RecipePanelContext>>
    ) {
        super.finishedRefresh(menuPanel, context, listItems)

        RecipesListContext.lastActiveRecipe?.let { recipe ->
            if (currContext.activeItem != recipe) {
                val recipeContext = listItems.firstOrNull { it.item == recipe }?.currContext
                if (recipeContext != null) {
                    this.pickedTab(recipeContext)
                } else {
                    context.activeItem = null
                }
            }
        }

        addListener {
            RecipesListContext.lastActiveRecipe = it.item
        }
    }

    override fun recreateTabHolder(
        menuPanel: CustomPanelAPI,
        context: TabListContext<Recipe, RecipePanelContext>
    ): CustomPanelAPI {
        return super.recreateTabHolder(menuPanel, context).also {
            it.position?.setYAlignOffset(30f)
        }
    }

    override fun getFilters(): List<String>? {
        return RecipeManager.getRecipesByHint().keys.filterNot { it.isEmpty() }.toList()
    }

    override fun getFiltersFromItem(item: Recipe): List<String> {
        return item.getHints()
    }
}

class RecipesListContext(val craftingContext: CraftingPanelContext) :
    FilteredTabListContext<Recipe, RecipePanelContext>() {
    companion object {
        var lastActiveRecipe: Recipe? = null
    }

    init {
        RecipeManager.getRecipes().forEach {
            if (it.canShow())
                tabs.add(RecipeItemContext(it, RecipePanelContext(it, craftingContext.fleet, craftingContext.market)))
        }
    }

    override fun sortItems(items: List<Recipe>): List<Recipe> {
        return items.sortedBy { it.getName() }
    }
}
