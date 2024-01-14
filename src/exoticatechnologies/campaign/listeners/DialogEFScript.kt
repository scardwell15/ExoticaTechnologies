package exoticatechnologies.campaign.listeners

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseStoryPointActionDelegate
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.StoryPointActionDelegate
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.campaign.ScanUtils
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.util.StringUtils
import exoticatechnologies.util.getChildrenCopy
import exoticatechnologies.util.getParent
import exoticatechnologies.util.reflect.ReflectionUtils
import lombok.extern.log4j.Log4j
import org.lwjgl.input.Keyboard
import java.awt.Color

@Log4j
class DialogEFScript : EveryFrameScript {
    companion object {
        var hack: TooltipMakerAPI? = null
            get() {
                val temp = field
                field = null
                return temp
            }
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        hack?.let { story ->
            ReflectionUtils.invoke(
                "getListener",
                story.getParent().getChildrenCopy().filterIsInstance<ButtonAPI>().first { it.text == "Cancel" })
                ?.let { cancel ->
                    ReflectionUtils.invoke("dismiss", cancel, 1)
                }
            hack = null
        }

        if (Global.getSector().campaignUI.isShowingDialog) {
            val dialog = Global.getSector().campaignUI.currentInteractionDialog
            if (dialog != null) {
                val plugin = dialog.plugin
                if (plugin is FleetInteractionDialogPluginImpl) {
                    if (!dialog.optionPanel.hasOption(FleetInteractionDialogPluginImpl.OptionId.ENGAGE)) return
                    if (dialog.optionPanel.hasOption("scan")) return
                    val context = plugin.context as FleetEncounterContext
                    if (context.battle.nonPlayerCombined.fleetData.membersListCopy.none { ShipModLoader.get(it, it.variant) != null }) return

                    //put above LEAVE, or at end of list.
                    var optionIndex = dialog.optionPanel.savedOptionList.size - 1
                    if (!dialog.optionPanel.hasOption(FleetInteractionDialogPluginImpl.OptionId.LEAVE)) {
                        optionIndex = dialog.optionPanel.savedOptionList.size
                    }

                    dialog.optionPanel.addOptionAtIndexWithHandler(
                        optionIndex,
                        StringUtils.getString("FleetScanner", "FleetScanOption"),
                        "scan",
                        tooltip = null
                    ) {
                        ScanUtils.showNotableShipsPanel(
                            dialog,
                            context.battle.nonPlayerCombined.fleetData.membersListCopy
                        )
                    }
                }
            }
        }
    }
}

fun OptionPanelAPI.addOptionAtIndexWithHandler(
    index: Int,
    text: String,
    data: Any,
    color: Color = Misc.getBasePlayerColor(),
    tooltip: String?,
    delegate: () -> Unit
) {
    this.addOption(
        text,
        data,
        color,
        tooltip
    )

    val originalOptionMap = ReflectionUtils.invoke("getButtonToItemMap", this) as MutableMap<Any?, Any?>
    val copiedOptionMap: MutableMap<Any?, Any?> = linkedMapOf()
    originalOptionMap.entries.forEach { (val1, val2) ->
        copiedOptionMap.put(val1, val2)
    }

    val originalOptions = this.savedOptionList
    var optionAdded = false
    val newOptions: MutableList<Any?> = mutableListOf()
    for (i in 0 until originalOptions.size - 1) {
        if (i == index) {
            newOptions.add(originalOptions.last())
            optionAdded = true
        }
        newOptions.add(originalOptions[i])
    }

    if (!optionAdded) {
        newOptions.add(originalOptions.last())
    }

    this.restoreSavedOptions(newOptions)
    resetOptionHotkeys(copiedOptionMap, this)
    resetOptionConfirmationDialogs(copiedOptionMap, this)

    this.addOptionConfirmation(
        data, AutoClosingOptionDelegate(delegate)
    )
}

fun resetOptionHotkeys(oldOptions: Map<Any?, Any?>, options: OptionPanelAPI) {
    val dataMethodName = ReflectionUtils.getMethodOfReturnType(oldOptions.values.first()!!, Object::class.java)!!
    val optionMap = ReflectionUtils.invoke("getButtonToItemMap", options) as MutableMap<Any?, Any?>
    for (newOption in optionMap.values) {
        newOption!!
        val newOptionData = ReflectionUtils.invoke(dataMethodName, newOption)
        oldOptions
            .filter { (_, optionObj) ->
                ReflectionUtils.invoke(dataMethodName, optionObj!!) == newOptionData
            }
            .entries
            .first()
            .let { (keyObj, optionObj) ->
                keyObj!!
                optionObj!!
                val firstArgClassOfAltShortcut = ReflectionUtils.getMethodArguments("setAltShortcut", keyObj)!![0]
                val optionHandlingScriptField = ReflectionUtils.findFieldWithMethodName(keyObj, "focusLost")!!
                val optionHandlingScriptObj = optionHandlingScriptField.get(keyObj)!!
                val keyHandlerFields =
                    ReflectionUtils.findFieldsOfType(optionHandlingScriptObj, firstArgClassOfAltShortcut)
                keyHandlerFields
                    .mapNotNull { it.get(optionHandlingScriptObj) }
                    .forEach { keyHandlerObj ->
                        val keyField = ReflectionUtils.findFieldsOfType(keyHandlerObj, Int::class.java)[0]
                        val actualKey = keyField.get(keyHandlerObj) as Int
                        if (!(Keyboard.KEY_1..Keyboard.KEY_9).contains(actualKey)) {
                            options.setShortcut(
                                newOptionData,
                                actualKey,
                                false,
                                false,
                                false,
                                true
                            )
                        }
                    }

            }
    }
}

fun resetOptionConfirmationDialogs(oldOptions: Map<Any?, Any?>, options: OptionPanelAPI) {
    val nameMethodName = ReflectionUtils.getMethodOfReturnType(oldOptions.values.first()!!, String::class.java)!!
    val dataMethodName = ReflectionUtils.getMethodOfReturnType(oldOptions.values.first()!!, Object::class.java)!!
    val optionMap = ReflectionUtils.invoke("getButtonToItemMap", options) as MutableMap<Any?, Any?>
    for (newOption in optionMap.values) {
        newOption!!
        val newOptionName = ReflectionUtils.invoke(nameMethodName, newOption)
        val newOptionData = ReflectionUtils.invoke(dataMethodName, newOption)
        oldOptions
            .filter { (_, optionObj) ->
                ReflectionUtils.invoke(dataMethodName, optionObj!!) == newOptionData
            }
            .entries
            .first()
            .let { (keyObj, optionObj) ->
                optionObj!!
                val confirmationStringFields = ReflectionUtils.findFieldsOfType(optionObj, String::class.java)
                    .map { it.get(optionObj) as String? }
                    .filterNotNull()
                    .filter { it != newOptionName }
                if (confirmationStringFields.size >= 3) {
                    val confirmationStringText: String
                    val confirmationStringYes: String
                    val confirmationStringNo: String
                    if (confirmationStringFields.size == 4) {
                        confirmationStringText = confirmationStringFields[1]
                        confirmationStringYes = confirmationStringFields[2]
                        confirmationStringNo = confirmationStringFields[3]
                    } else {
                        confirmationStringText = confirmationStringFields[0]
                        confirmationStringYes = confirmationStringFields[1]
                        confirmationStringNo = confirmationStringFields[2]
                    }
                    options.addOptionConfirmation(
                        newOptionData,
                        confirmationStringText,
                        confirmationStringYes,
                        confirmationStringNo
                    )
                }

                val confirmationStoryField =
                    ReflectionUtils.findFieldsOfType(optionObj, StoryPointActionDelegate::class.java)[0]
                val confirmationStoryDelegate = confirmationStoryField.get(optionObj) as StoryPointActionDelegate?
                if (confirmationStoryDelegate != null) {
                    options.addOptionConfirmation(newOptionData, confirmationStoryDelegate)
                }
            }
    }
}

//Thanks AtlanticAccent
open class AutoClosingOptionDelegate(val block: () -> Unit) :
    BaseStoryPointActionDelegate() {

    override fun getRequiredStoryPoints(): Int = 0

    override fun withSPInfo(): Boolean = false

    override fun createDescription(info: TooltipMakerAPI) {
        block()
        DialogEFScript.hack = info
    }

    override fun getLogText(): String =
        "Why are we still here? Just to suffer? Every night, I can feel my leg... And my arm... even my fingers... The body I've lost... the comrades I've lost... won't stop hurting... It's like they're all still there. You feel it, too, don't you? I'm gonna make them give back our past!"
}