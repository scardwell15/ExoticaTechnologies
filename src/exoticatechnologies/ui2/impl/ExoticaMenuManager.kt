package exoticatechnologies.ui2.impl

object ExoticaMenuManager {
    @JvmStatic
    val modMenuPanels: MutableList<ExoticaTabContext> = mutableListOf()

    @JvmStatic
    fun addMenu(plugin: ExoticaTabContext) {
        modMenuPanels.add(plugin)
    }
}
