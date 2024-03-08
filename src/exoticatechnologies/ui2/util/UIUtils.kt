package exoticatechnologies.ui2.util

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.ui2.SpritePanelPlugin
import exoticatechnologies.util.reflect.ReflectionUtils
import java.awt.Color

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
        var actualX = xOffset
        var actualY = yOffset
        ReflectionUtils.getMethodOfNameInClass("setOffset", tooltip.externalScroller::class.java as Class<Any>)!!.invoke(tooltip.externalScroller,
            actualX,
            actualY)
    }

    fun addSpriteIconWithOverlay(tooltip: TooltipMakerAPI, spriteName: String, overlaySpriteName: String, overlaySpriteColor: Color): Pair<SpritePanelPlugin, SpritePanelPlugin> {
        val spritePlugin = SpritePanelPlugin(Global.getSettings().getSprite(spriteName))
        val iconPanel = Global.getSettings().createCustom(64f, 64f, spritePlugin)
        spritePlugin.panel = iconPanel
        tooltip.addCustom(iconPanel, 0f)

        val typeOverlay: SpritePanelPlugin = addSpriteOverlayOver(tooltip, iconPanel, overlaySpriteName, overlaySpriteColor)

        return spritePlugin to typeOverlay
    }

    fun addSpriteOverlayOver(tooltip: TooltipMakerAPI, iconComponent: UIComponentAPI, spriteName: String, spriteColor: Color): SpritePanelPlugin {
        val sprite = Global.getSettings().getSprite(spriteName)
        sprite.color = spriteColor
        val typeOverlay = SpritePanelPlugin(sprite)
        val overlayPanel =
            Global.getSettings().createCustom(iconComponent.position.width, iconComponent.position.height, typeOverlay)
        tooltip.addCustom(overlayPanel, -iconComponent.position.height)

        return typeOverlay
    }
}