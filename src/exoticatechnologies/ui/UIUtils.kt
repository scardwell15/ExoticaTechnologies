package exoticatechnologies.ui

import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.util.reflect.MethodWrapperKT
import exoticatechnologies.util.reflect.ReflectionUtil

object UIUtils {
    private var resizeMethod: MethodWrapperKT? = null

    /**
     * resize the tooltip automatically. no idea why this isn't exposed.
     */
    fun autoResize(tooltip: TooltipMakerAPI) {
        if (resizeMethod == null) {
            resizeMethod = ReflectionUtil.getObjectMethodWrapper(tooltip, "updateSizeAsUIElement", tooltip.javaClass)
        }
        resizeMethod!!.invoke(tooltip)
    }
}