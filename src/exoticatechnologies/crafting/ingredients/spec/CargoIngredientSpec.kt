package exoticatechnologies.crafting.ingredients.spec

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.cargo.CrateGlobalData
import exoticatechnologies.crafting.ingredients.CargoMaterial
import exoticatechnologies.crafting.ingredients.Ingredient

abstract class CargoIngredientSpec<T : CargoMaterial>(protected val reqAmount: Float) : IngredientSpec<T> {
    override fun getRequired(): Float {
        return reqAmount
    }

    abstract fun specMatchesStack(stack: CargoStackAPI): Boolean

    abstract fun createIngredientForStack(stack: CargoStackAPI, source: IngredientSource): T

    override fun hasRequiredAmount(ingredients: List<Ingredient>): Boolean {
        return ingredients.sumOf { (it as CargoMaterial).stack.size.toDouble() } >= reqAmount
    }

    override fun canUseIngredientForStack(ingredient: Ingredient): Boolean {
        return (ingredient as CargoMaterial).stack.size > 0
    }

    override fun createIngredients(fleet: CampaignFleetAPI, market: MarketAPI?): List<T> {
        val ingredients = mutableListOf<T>()
        ingredients.addAll(createIngredientsFromCargo(fleet.cargo, IngredientSource.FLEET))
        ingredients.addAll(createIngredientsFromCargo(CrateGlobalData.getInstance().cargo, IngredientSource.CRATE))

        market?.let {
            Misc.getStorageCargo(it)?.let {
                ingredients.addAll(createIngredientsFromCargo(it, IngredientSource.STORAGE))
            }
        }

        return ingredients
    }

    fun createIngredientsFromCargo(cargo: CargoAPI, source: IngredientSource): List<T> {
        return cargo.stacksCopy
            .filter { specMatchesStack(it) }
            .map { createIngredientForStack(it, source) }
    }

    override fun getFiltersForIngredients(selectedIngredients: List<Ingredient>): List<String> {
        return selectedIngredients
            .flatMap { getFiltersForIngredient(it) }
            .distinct()
    }

    override fun getFiltersForIngredient(ingredient: Ingredient): List<String> {
        ingredient as CargoMaterial
        val filters: MutableList<String> = mutableListOf()

        if (ingredient.stack.isCommodityStack) {
            filters.add(ingredient.stack.displayName)
        } else if (ingredient.stack.isSpecialStack) {
            filters.add("Special")
        } else if (ingredient.stack.isWeaponStack) {
            filters.add("Weapons")
        } else if (ingredient.stack.isFighterWingStack) {
            filters.add("Fighters")
        }

        return filters
    }
}