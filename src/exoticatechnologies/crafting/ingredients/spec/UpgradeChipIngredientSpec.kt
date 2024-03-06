package exoticatechnologies.crafting.ingredients.spec

import exoticatechnologies.crafting.ingredients.CargoMaterial
import exoticatechnologies.crafting.ingredients.Ingredient
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin

open class UpgradeChipIngredientSpec(specialId: String, specialData: String?, reqAmount: Float) :
    SpecialItemIngredientSpec(
        specialId, specialData,
        reqAmount
    ) {

    constructor(upgrade: Upgrade, reqAmount: Float) : this(Upgrade.ITEM, "/${upgrade.key}", reqAmount)
    constructor(upgradeData: String, reqAmount: Float) : this(Upgrade.ITEM, upgradeData, reqAmount)

    override fun sortIngredients(ingredients: List<Ingredient>): List<Ingredient> {
        ingredients as List<CargoMaterial>

        return ingredients.sortedWith { a, b ->
            val aPlugin = a.stack.plugin as UpgradeSpecialItemPlugin
            val bPlugin = b.stack.plugin as UpgradeSpecialItemPlugin
            val aUpgrade = aPlugin.upgrade!!
            val aLevel = aPlugin.upgradeLevel
            val bUpgrade = bPlugin.upgrade!!
            val bLevel = bPlugin.upgradeLevel

            val aIndex = aUpgrade.getIndex()
            val bIndex = bUpgrade.getIndex()
            if (aIndex == bIndex) {
                aLevel - bLevel
            } else {
                aIndex - bIndex
            }
        }
    }


    override fun getItemName(ingredient: CargoMaterial?): String {
        ingredient?.let {
            val plugin = ingredient.stack.plugin as UpgradeSpecialItemPlugin
            return "${plugin.upgrade!!.name} (${plugin.upgradeLevel})"
        }
        return super.getItemName(ingredient)
    }

    override fun getIconSprite(ingredient: CargoMaterial?): String {
        ingredient?.let {
            val plugin = ingredient.stack.plugin as UpgradeSpecialItemPlugin
            return plugin.upgrade!!.iconPath
        }
        return super.getIconSprite(ingredient)
    }

    override fun getFiltersForIngredient(ingredient: Ingredient): List<String> {
        ingredient as CargoMaterial
        val filters: MutableList<String> = mutableListOf()
        filters.add((ingredient.stack.plugin as UpgradeSpecialItemPlugin).upgrade!!.name)
        return filters
    }
}