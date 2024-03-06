package exoticatechnologies.crafting.ingredients.spec

import exoticatechnologies.crafting.ingredients.CargoMaterial
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin

open class ExoticChipIngredientSpec(specialId: String, specialData: String?, reqAmount: Float) :
    SpecialItemIngredientSpec(specialId, specialData, reqAmount) {
    constructor(exotic: Exotic, reqAmount: Float) : this(Exotic.ITEM, "/${exotic.name}", reqAmount)
    constructor(exoticData: String, reqAmount: Float) : this(Exotic.ITEM, exoticData, reqAmount)

    override fun getIconSprite(ingredient: CargoMaterial?): String {
        ingredient?.let {
            return (ingredient.stack.plugin as ExoticSpecialItemPlugin).exotic!!.iconPath
        }
        return super.getIconSprite(ingredient)
    }

    override fun getFiltersForIngredient(ingredient: Ingredient): List<String> {
        ingredient as CargoMaterial
        val filters: MutableList<String> = mutableListOf()
        filters.add((ingredient.stack.plugin as ExoticSpecialItemPlugin).exotic!!.name)
        (ingredient.stack.plugin as ExoticSpecialItemPlugin).exoticData?.type?.let {
            filters.add(it.name)
        }
        return filters
    }
}