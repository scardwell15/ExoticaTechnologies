package exoticatechnologies.ui.impl.shop

class ShopManager {
    companion object {
        @JvmStatic
        val shopMenuUIPlugins: MutableList<ShopMenuUIPlugin> = mutableListOf()

        @JvmStatic
        fun addMenu(plugin: ShopMenuUIPlugin) {
            shopMenuUIPlugins.add(plugin)
        }
    }
}