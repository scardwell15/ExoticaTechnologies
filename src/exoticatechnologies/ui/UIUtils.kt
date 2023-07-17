package exoticatechnologies.ui

import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.util.reflect.MethodWrapperKT
import exoticatechnologies.util.reflect.ReflectionUtil

object UIUtils {
    /**
     * resize the tooltip automatically. no idea why this isn't exposed.
     */
    fun autoResize(tooltip: TooltipMakerAPI) {
        ReflectionUtil.getObjectMethodWrapper(tooltip, "updateSizeAsUIElement", tooltip.javaClass)!!.invoke(tooltip)
    }
}