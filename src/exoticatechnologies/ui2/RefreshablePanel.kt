package exoticatechnologies.ui2

import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.roundToInt

open class RefreshablePanel<T : PanelContext>(context: T) : InteractivePanel<T>(context) {
    private var basePanel: CustomPanelAPI? = null
    private var menuPanel: CustomPanelAPI? = null
    open var innerPadding: Float = 4f
    open var outerPadding: Float = 4f
    val innerWidth: Float
        get() = panelWidth - innerPadding * 2f
    val innerHeight: Float
        get() = panelHeight - innerPadding * 2f
    val preRefreshes: MutableList<(T, CustomPanelAPI?) -> Unit> = mutableListOf()
    val postRefreshes: MutableList<(T, CustomPanelAPI?) -> Unit> = mutableListOf()

    fun layoutPanel(holdingPanel: CustomPanelAPI, context: T?): CustomPanelAPI {
        if (basePanel != null) {
            destroyPanel()
        }

        basePanel = holdingPanel

        return refreshPanel(context)
    }

    fun destroyPanel() {
        if (menuPanel != null) {
            basePanel?.removeComponent(menuPanel)
        }
        menuPanel = null
    }

    fun refreshPanel(context: T? = currContext): CustomPanelAPI {
        if (basePanel == null) {
            throw RuntimeException("basePanel was not set using layoutPanel.")
        }

        val preRefreshesCopy = ArrayList(preRefreshes)
        val postRefreshesCopy = ArrayList(postRefreshes)

        preRefreshes.clear()
        postRefreshes.clear()

        preRefreshesCopy.forEach { it.invoke(context ?: currContext, menuPanel) }
        destroyPanel()
        clearEventHandlers()

        if (context != null) {
            currContext = context
        }

        panelWidth = panelWidth.roundToInt().toFloat()
        panelHeight = panelHeight.roundToInt().toFloat()

        menuPanel = basePanel?.createCustomPanel(panelWidth, panelHeight, this)
        basePanel?.addComponent(menuPanel)?.inTL(outerPadding, outerPadding)

        setupInteraction(menuPanel!!)

        refresh(menuPanel!!, currContext)
        postRefreshesCopy.forEach { it.invoke(context ?: currContext, menuPanel) }


        return menuPanel!!
    }

    fun getPanel(): CustomPanelAPI? {
        return menuPanel
    }

    protected open fun refresh(menuPanel: CustomPanelAPI, context: T) {

    }

    fun preRefresh(handler: (T, CustomPanelAPI?) -> Unit) {
        preRefreshes.add(handler)
    }

    fun postRefresh(handler: (T, CustomPanelAPI?) -> Unit) {
        postRefreshes.add(handler)
    }
}