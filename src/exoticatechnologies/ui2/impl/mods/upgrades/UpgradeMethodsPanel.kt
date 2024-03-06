package exoticatechnologies.ui2.impl.mods.upgrades

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradesHandler
import exoticatechnologies.ui2.util.StringTooltip
import exoticatechnologies.ui2.impl.mods.upgrades.methods.ChipMethod
import exoticatechnologies.ui2.impl.mods.upgrades.methods.UpgradeMethod
import exoticatechnologies.ui2.impl.ExoticaPanelContext
import exoticatechnologies.ui2.impl.mods.ExoticaPanel
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.safeLet
import java.awt.Color

class UpgradeMethodsPanel(
    var upgrade: Upgrade,
    context: ExoticaPanelContext
) : ExoticaPanel(context) {
    private var listener = MethodComponentHandlers()

    override fun refresh(menuPanel: CustomPanelAPI, context: ExoticaPanelContext) {
        val tooltip = menuPanel.createUIElement(panelWidth, panelHeight, false)

        var prev: UIComponentAPI? = null
        safeLet(member, variant, mods) { member, variant, mods ->
            if (mods.isMaxLevel(member, upgrade)) {
                tooltip.addTitle(StringUtils.getString("UpgradesDialog", "MaxLevelTitle"))
            } else if (!upgrade.canApplyImpl(member, variant, mods)) {
                tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))
                showCannotApply(member, mods, tooltip)

                prev = tooltip.prev
            } else if (!checkBandwidth(member, mods)) {
                tooltip.addTitle(StringUtils.getString("Conditions", "CannotApplyTitle"), Color(200, 100, 100))
                StringUtils.getTranslation("Conditions", "CannotApplyBecauseBandwidth")
                    .addToTooltip(tooltip)

                prev = tooltip.prev
            } else {
                tooltip.addTitle(StringUtils.getString("UpgradeMethods", "UpgradeMethodsTitle"))
            }
            showMethods(member, variant, mods, tooltip, prev)
        }

        menuPanel.addUIElement(tooltip).inTL(0f, 0f)
    }

    fun checkBandwidth(member: FleetMemberAPI, mods: ShipModifications): Boolean {
        val stack = ChipMethod.getDesiredChip(member, mods, upgrade)
        if (stack != null) {
            return true //usable chip
        } else {
            val shipBandwidth = mods.getBandwidthWithExotics(member)
            val usedBandwidth = mods.getUsedBandwidth()
            val upgradeUsage = upgrade.bandwidthUsage
            return usedBandwidth + upgradeUsage <= shipBandwidth
        }
    }

    fun showCannotApply(member: FleetMemberAPI, mods: ShipModifications, tooltip: TooltipMakerAPI) {
        val reasons: List<String> = upgrade.getCannotApplyReasons(member, mods)
        if (reasons.isNotEmpty()) {
            reasons.forEach {
                tooltip.addPara(it, 1f)
            }
        } else if (!upgrade.checkTags(member, mods, upgrade.tags)) {
            val names: List<String> = mods.getModsThatConflict(upgrade.tags).map { it.name }

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
        lastComponent: UIComponentAPI? = null
    ) {
        //this list automatically places buttons on new rows if the previous row had too many
        var lastButton: UIComponentAPI? = null
        var nextButtonX = 0f
        var rowYOffset = 25f

        if (lastComponent != null) {
            rowYOffset += lastComponent.position.height
        }

        for (method in UpgradesHandler.UPGRADE_METHODS) {
            if (!method.canShow(member, variant, mods, upgrade, market)) continue

            val buttonText = method.getOptionText(member, mods, upgrade, market)
            tooltip.setButtonFontDefault()

            val buttonWidth: Float = tooltip.computeStringWidth(buttonText) + 16f
            if (nextButtonX + buttonWidth >= panelWidth) {
                nextButtonX = 0f
                rowYOffset += 24f
                lastButton = null
            }

            if (upgrade.canUseUpgradeMethod(member, mods, method)) {
                val methodButton: ButtonAPI = tooltip.addButton(buttonText, "", buttonWidth, 18f, 2f)
                val tooltipText = method.getOptionTooltip(member, variant, mods, upgrade, market)
                if (tooltipText != null) {
                    tooltip.addTooltipToPrevious(
                        StringTooltip(tooltip, tooltipText),
                        TooltipMakerAPI.TooltipLocation.BELOW
                    )
                }

                methodButton.isEnabled = (market != null || method.canUseIfMarketIsNull()) && method.canUse(
                    member,
                    variant,
                    mods,
                    upgrade,
                    market
                )

                if (lastButton == null) {
                    methodButton.position.inTL(0f, rowYOffset)
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

    fun onMethodClicked(method: (UpgradeMethod) -> Unit) {
        listener.clicks.add(method)
    }

    fun onMethodHighlighted(method: (UpgradeMethod) -> Unit) {
        listener.mouseEnters.add(method)
    }

    fun onMethodUnhighlighted(method: (UpgradeMethod) -> Unit) {
        listener.mouseExits.add(method)
    }

    fun callListenerHighlighted(method: UpgradeMethod) {
        listener.mouseEntered(method)
    }

    fun callListenerUnhighlighted(method: UpgradeMethod) {
        listener.mouseExited(method)
    }

    fun callListenerChecked(method: UpgradeMethod) {
        listener.clicked(method)
    }
}


fun interface MethodClickHandler {
    fun checked(method: UpgradeMethod)
}

fun interface MethodMouseEnterHandler {
    fun mouseEntered(method: UpgradeMethod)
}

fun interface MethodMouseExitHandler {
    fun mouseExited(method: UpgradeMethod)
}

class MethodComponentHandlers {
    val clicks = mutableListOf<MethodClickHandler>()
    val mouseEnters = mutableListOf<MethodMouseEnterHandler>()
    val mouseExits = mutableListOf<MethodMouseExitHandler>()

    fun clicked(method: UpgradeMethod) {
        clicks.forEach { it.checked(method) }
    }

    fun mouseEntered(method: UpgradeMethod) {
        mouseEnters.forEach { it.mouseEntered(method) }
    }

    fun mouseExited(method: UpgradeMethod) {
        mouseExits.forEach { it.mouseExited(method) }
    }
}