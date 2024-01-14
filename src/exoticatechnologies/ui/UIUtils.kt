package exoticatechnologies.ui

import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.util.reflect.ReflectionUtils

object UIUtils {
    /**
     * resize the tooltip automatically. no idea why this isn't exposed.
     */
    fun autoResize(tooltip: TooltipMakerAPI) {
        ReflectionUtils.getMethod("updateSizeAsUIElement", tooltip)?.invoke(tooltip)
    }
}