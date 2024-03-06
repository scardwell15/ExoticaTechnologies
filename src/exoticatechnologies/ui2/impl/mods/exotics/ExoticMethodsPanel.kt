package exoticatechnologies.ui2.impl.mods.exotics

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticsHandler
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.ui2.impl.mods.exotics.methods.ExoticMethod
import exoticatechnologies.ui2.util.StringTooltip
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet
import java.awt.Color

class ExoticMethodsPanel(
    var exotic: Exotic,
    context: ExoticaPanelContext
) : ExoticaPanel(context) {
    private var listener = MethodComponentHandlers()

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)

        safeLet(member, variant, mods) { member, variant, mods ->
            if (mods.hasExotic(exotic)) {
                tooltip.addTitle(StringUtils.getString("ExoticsDialog", "InstalledTitle"))
            } else if (!exotic.canApplyImpl(member, variant, mods)) {
                tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))
                showCannotApply(member, mods, tooltip)
            } else if (!mods.isUnderExoticLimit(member)) {
                tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))

                StringUtils.getTranslation("Conditions", "CannotApplyBecauseTooManyExotics")
                    .addToTooltip(tooltip, Color(100, 200, 100))
            } else {
                tooltip.addTitle(StringUtils.getString("UpgradeMethods", "UpgradeMethodsTitle"))
            }

            showMethods(member, variant, mods, tooltip)
        }

        menuPanel.addUIElement(tooltip).inTL(0f, 0f)
    }

    fun showCannotApply(member: FleetMemberAPI, mods: ShipModifications, tooltip: TooltipMakerAPI) {
        val reasons: List<String> = exotic.getCannotApplyReasons(member, mods)
        if (reasons.isNotEmpty()) {
            reasons.forEach {
                tooltip.addPara(it, 1f)
            }
        } else if (!exotic.checkTags(member, mods, exotic.tags)) {
            val names: List<String> = mods.getModsThatConflict(exotic.tags).map { it.name }

            StringUtils.getTranslation("Conditions", "CannotApplyBecauseTags")
                .format("conflictMods", names.joinToString(", "))
                .addToTooltip(tooltip)
        }
    }

    fun showMethods(
        member: FleetMemberAPI,
        variant: ShipVariantAPI,
        mods: ShipModifications,
        tooltip: TooltipMakerAPI,
        lastComponent: UIComponentAPI? = tooltip.prev
    ) {
        //this list automatically places buttons on new rows if the previous row had too many
        var lastButton: UIComponentAPI? = null
        var nextButtonX = 0f
        var rowYOffset = innerPadding

        for (method in ExoticsHandler.EXOTIC_METHODS) {
            if (!method.canShow(member, variant, mods, exotic, market)) continue

            val buttonText = method.getButtonText(exotic)
            tooltip.setButtonFontDefault()

            val buttonWidth: Float = tooltip.computeStringWidth(buttonText) + 16f
            if (nextButtonX + buttonWidth >= panelWidth) {
                nextButtonX = 0f
                rowYOffset += 24f
                lastButton = null
            }

            if (method.canShow(member, variant, mods, exotic, market) && exotic.canUseMethod(member, mods, method)) {
                val methodButton: ButtonAPI = tooltip.addButton(buttonText, "", buttonWidth, 18f, 2f)
                method.getButtonTooltip(exotic)?.let {
                    tooltip.addTooltipToPrevious(
                        StringTooltip(tooltip, it),
                        TooltipMakerAPI.TooltipLocation.BELOW
                    )
                }

                methodButton.isEnabled = (market != null || method.canUseIfMarketIsNull()) && method.canUse(
                    member,
                    variant,
                    mods,
                    exotic,
                    market
                )

                if (lastButton == null) {
                    if (lastComponent != null) {
                        methodButton.position.belowLeft(lastComponent, rowYOffset)
                    } else {
                        methodButton.position.inTL(0f, rowYOffset)
                    }
                } else {
                    methodButton.position.rightOfTop(lastButton, 3f)
                }
                lastButton = methodButton
                nextButtonX += buttonWidth + 3f

                onClick(methodButton) {
                    if (!methodButton.isEnabled) return@onClick
                    callListenerChecked(method)
                }

                onMouseEnter(methodButton) {
                    callListenerHighlighted(method)
                }

                onMouseExit(methodButton) {
                    callListenerUnhighlighted(method)
                }
            }
        }
    }

    fun onMethodClicked(method: (ExoticMethod) -> Unit) {
        listener.clicks.add(method)
    }

    fun onMethodHighlighted(method: (ExoticMethod) -> Unit) {
        listener.mouseEnters.add(method)
    }

    fun onMethodUnhighlighted(method: (ExoticMethod) -> Unit) {
        listener.mouseExits.add(method)
    }

    fun callListenerHighlighted(method: ExoticMethod) {
        listener.mouseEntered(method)
    }

    fun callListenerUnhighlighted(method: ExoticMethod) {
        listener.mouseExited(method)
    }

    fun callListenerChecked(method: ExoticMethod) {
        listener.clicked(method)
    }
}


fun interface MethodClickHandler {
    fun checked(method: ExoticMethod)
}

fun interface MethodMouseEnterHandler {
    fun mouseEntered(method: ExoticMethod)
}

fun interface MethodMouseExitHandler {
    fun mouseExited(method: ExoticMethod)
}

class MethodComponentHandlers {
    val clicks = mutableListOf<MethodClickHandler>()
    val mouseEnters = mutableListOf<MethodMouseEnterHandler>()
    val mouseExits = mutableListOf<MethodMouseExitHandler>()

    fun clicked(method: ExoticMethod) {
        clicks.forEach { it.checked(method) }
    }

    fun mouseEntered(method: ExoticMethod) {
        mouseEnters.forEach { it.mouseEntered(method) }
    }

    fun mouseExited(method: ExoticMethod) {
        mouseExits.forEach { it.mouseExited(method) }
    }
}