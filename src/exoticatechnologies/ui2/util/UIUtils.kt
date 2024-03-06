package exoticatechnologies.ui2.util

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.util.reflect.ReflectionUtils

object UIUtils {
    /**
     * resize the tooltip automatically. no idea why this isn't exposed.
     */
    fun autoResize(tooltip: TooltipMakerAPI) {
        ReflectionUtils.getMethod("updateSizeAsUIElement", tooltip)?.invoke(tooltip)
    }

    fun smoothScrollToElement(tooltip: TooltipMakerAPI, element: UIComponentAPI) {
        ReflectionUtils.getMethodOfNameInClass("ensureVisible", tooltip.externalScroller::class.java as Class<Any>)!!.invoke(tooltip.externalScroller, element)
    }

    fun scrollToElement(tooltip: TooltipMakerAPI, element: UIComponentAPI) {
        ReflectionUtils.getMethodOfNameInClass("ensureVisible", tooltip.externalScroller::class.java as Class<Any>)!!.invoke(tooltip.externalScroller, element)
        ReflectionUtils.getMethodOfNameInClass("setOffset", tooltip.externalScroller::class.java as Class<Any>)!!.invoke(tooltip.externalScroller, tooltip.externalScroller.xOffset, tooltip.externalScroller.yOffset)
    }

    fun scrollTo(tooltip: TooltipMakerAPI, xOffset: Float = tooltip.externalScroller.xOffset, yOffset: Float = tooltip.externalScroller.yOffset) {
        ReflectionUtils.getMethodOfNameInClass("setOffset", tooltip.externalScroller::class.java as Class<Any>)!!.invoke(tooltip.externalScroller, xOffset, yOffset)
    }
}