package exoticatechnologies.modifications.exotics.types

import com.fs.starfarer.api.Global
import exoticatechnologies.ui.SpritePanelPlugin
import org.magiclib.kotlin.setAlpha

class ExoticTypePanelPlugin(val type: ExoticType) : SpritePanelPlugin(Global.getSettings().getSprite(type.sprite)) {
    init {
        sprite.color = type.colorOverlay.setAlpha(255)
    }
}