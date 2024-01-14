package exoticatechnologies.refit

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.loading.specs.HullVariantSpec
import com.fs.state.AppDriver
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaUI.elements.LunaSpriteElement
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class RefitButtonAdder : EveryFrameScript {
    var refitPanel: UIPanelAPI? = null
    var shipDisplay: UIPanelAPI? = null
    var modWidget: UIPanelAPI? = null
    var openButtonPanel: CustomPanelAPI? = null
    var closeButtonPanel: CustomPanelAPI? = null

    var firstButtonLoad = true

    companion object {
        var member: FleetMemberAPI? = null
        var variant: HullVariantSpec? = null
        var requiresVariantUpdate = false
    }

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

    private val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    private val getMethodNameHandle =
        MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    private val invokeMethodHandle = MethodHandles.lookup().findVirtual(
        methodClass,
        "invoke",
        MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java)
    )

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.REFIT) {
            refitPanel = null
            shipDisplay = null
            modWidget = null
            openButtonPanel = null
            closeButtonPanel = null
            member = null
            variant = null
            return
        }

        var state = AppDriver.getInstance().currentState

        //Makes sure that the current state is the campaign state.
        if (state !is CampaignState) return

        var modWidgetLocal: UIPanelAPI? = null
        var buildButton: UIComponentAPI? = null

        var core = invokeMethod("getCore", state)

        var dialog = invokeMethod("getEncounterDialog", state)
        if (dialog != null) {
            core = invokeMethod("getCoreUI", dialog)
        }

        if (core is UIPanelAPI) {
            var child1 = core.getChildrenCopy().find { hasMethodOfName("setBorderInsetLeft", it) }
            if (child1 is UIPanelAPI) {
                var child2 = child1.getChildrenCopy().find { hasMethodOfName("goBackToParentIfNeeded", it) }

                if (child2 is UIPanelAPI) {
                    var child3 =
                        child2.getChildrenCopy().find { hasMethodOfName("syncWithCurrentVariant", it) } as UIPanelAPI?

                    if (child3 is UIPanelAPI) {
                        refitPanel = child3
                        member = getMember()
                        if (member == null) //shipName check catches modules
                        {
                            removeExoticaButton()
                            return
                        }

                        shipDisplay = invokeMethod("getShipDisplay", refitPanel!!) as UIPanelAPI?
                        variant = invokeMethod("getCurrentVariant", shipDisplay!!) as HullVariantSpec?

                        var child4 = child3.getChildrenCopy().find { hasMethodOfName("getColorFor", it) } as UIPanelAPI?

                        if (child4 is UIPanelAPI) {
                            modWidgetLocal = child4.getChildrenCopy()
                                .find { hasMethodOfName("removeNotApplicableMods", it) } as UIPanelAPI?

                            if (modWidgetLocal is UIPanelAPI) {
                                modWidget = modWidgetLocal
                                if (modWidgetLocal.getChildrenCopy().any { it == openButtonPanel }) {
                                    return
                                }

                                var buttons = modWidgetLocal.getChildrenCopy().filter { hasMethodOfName("getText", it) }

                                buildButton = buttons.find {
                                    (invokeMethod(
                                        "getText",
                                        it
                                    ) as String).contains("Build in")
                                } as UIComponentAPI
                            }
                        }
                    }
                }
            }
        }

        if (modWidgetLocal == null) {
            firstButtonLoad = true
        }

        if (modWidgetLocal != null && buildButton != null) {
            openButtonPanel =
                Global.getSettings().createCustom(buildButton.position.width, buildButton.position.height, null)
            modWidgetLocal.addComponent(openButtonPanel)
            openButtonPanel!!.position.belowLeft(buildButton, 3f)

            var openElement =
                openButtonPanel!!.createUIElement(buildButton.position.width, buildButton.position.height, false)
            var exoticaButtonPos = openElement.position.inTL(-5f, 0f)
            openButtonPanel!!.addUIElement(openElement)
            var openExoticaButton = openElement.addLunaSpriteElement(
                "graphics/ui/exoticaButton.png",
                LunaSpriteElement.ScalingTypes.STRETCH_SPRITE,
                buildButton.position.width,
                buildButton.position.height
            ).apply {
                enableTransparency = true
            }

            openElement.setParaFont(Fonts.ORBITRON_20AA)
            var openPara =
                openElement.addPara("Exotica", 0f, Color(215, 175, 255).setAlpha(0), Misc.getHighlightColor())

            openPara.position.inTL(
                buildButton.position.width / 2 - openPara.computeTextWidth(openPara.text) / 2 + 5,
                2f
            )

            openExoticaButton.getSprite().alphaMult = 0.8f

            openExoticaButton.onHoverEnter {
                openExoticaButton.getSprite().alphaMult = 1f
                openExoticaButton.playSound("ui_button_mouseover", 1f, 1f)
            }

            openExoticaButton.onHoverExit {
                openExoticaButton.getSprite().alphaMult = 0.8f
            }

            openExoticaButton.onClick {
                openExoticaButton.playClickSound()

                var corePanel = core
                if (dialog is UIPanelAPI) corePanel = dialog

                if (corePanel is UIPanelAPI) {
                    var plugin = ExoticaPanelPlugin(corePanel, member!!, this)

                    var width = CustomExoticaPanel.getWidth()
                    var height = CustomExoticaPanel.getHeight()
                    var x = exoticaButtonPos.x - 4f - width
                    var y = Global.getSettings().screenHeight / 2 - height / 2
                    while (x < 0) {
                        width *= 0.9f
                        x = exoticaButtonPos.x - 4f - width
                    }

                    var exoticaPanel = Global.getSettings().createCustom(width, height, plugin)
                    plugin.panel = exoticaPanel
                    corePanel.addComponent(exoticaPanel)

                    var custom = CustomExoticaPanel()
                    custom.x = x
                    custom.y = y
                    custom.init(exoticaPanel, plugin, width, height, member!!, variant!!)

                    closeButtonPanel =
                        Global.getSettings().createCustom(buildButton.position.width, buildButton.position.height, null)
                    plugin.closeButtonPanel = closeButtonPanel
                    exoticaPanel.addComponent(closeButtonPanel)
                    //turns out hierarchical siblings can be anchored on if you remember to remove them from their parent panel before removing the parent panel.
                    closeButtonPanel!!.position.belowLeft(buildButton, 3f).setXAlignOffset(-4f)

                    var closeElement = closeButtonPanel!!.createUIElement(
                        buildButton.position.width,
                        buildButton.position.height,
                        false
                    )
                    closeButtonPanel!!.addUIElement(closeElement)
                    var closeExoticaButton = closeElement.addLunaSpriteElement(
                        "graphics/ui/exoticaButton.png",
                        LunaSpriteElement.ScalingTypes.STRETCH_SPRITE,
                        buildButton.position.width,
                        buildButton.position.height
                    ).apply {
                        enableTransparency = true
                    }

                    closeElement.setParaFont(Fonts.ORBITRON_20AA)
                    var closePara = closeElement.addPara("Close", 0f, Color(215, 175, 255), Misc.getHighlightColor())

                    closePara.position.inTL(
                        buildButton.position.width / 2 - closePara.computeTextWidth(closePara.text) / 2 + 5,
                        2f
                    )

                    closeExoticaButton.getSprite().alphaMult = 0.8f

                    closeExoticaButton.onHoverEnter {
                        closeExoticaButton.getSprite().alphaMult = 1f
                        closeExoticaButton.playSound("ui_button_mouseover", 1f, 1f)
                    }

                    closeExoticaButton.onHoverExit {
                        closeExoticaButton.getSprite().alphaMult = 0.8f
                    }

                    closeExoticaButton.onClick {
                        plugin.close()
                    }
                }
            }

            if (firstButtonLoad) {
                openExoticaButton.getSprite().alphaMult = 0.0f
                openExoticaButton.advance {
                    var sprite = openExoticaButton.getSprite()
                    if (sprite.alphaMult < 0.8f) {
                        sprite.alphaMult += 0.1f

                        var colorAlpha = (150 * sprite.alphaMult) + 100
                        colorAlpha = MathUtils.clamp(colorAlpha, 0f, 254f)

                        openPara.setColor(Color(215, 175, 255).setAlpha(colorAlpha.toInt()))
                    }
                }
            }
        }
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any): Boolean {

        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
    }

    private fun getMethod(methodName: String, instance: Any, vararg arguments: Any?): Any? {
        var method: Any? = null
        val clazz = instance.javaClass

        try {
            val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
            val methodType = MethodType.methodType(Void.TYPE, args)

            method = clazz.getMethod(methodName, *methodType.parameterArray())
        } catch (_: Throwable) {
            for (currMethod: Any? in clazz.declaredMethods) {
                if (getMethodNameHandle.invoke(currMethod) != methodName) continue
                method = currMethod
                break
            }

            if (method == null) {
                for (currMethod: Any? in clazz.methods) {
                    if (getMethodNameHandle.invoke(currMethod) != methodName) continue
                    method = currMethod
                    break
                }
            }
        }

        return method
    }

    //Required to execute obfuscated methods without referencing their obfuscated class name.
    private fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?): Any? {
        return invokeMethodRef(getMethod(methodName, instance, *arguments), instance, *arguments)
    }

    private fun invokeMethodRef(method: Any?, instance: Any, vararg arguments: Any?): Any? {
        if (arguments.isEmpty()) {
            return invokeMethodHandle.invoke(method, instance)
        }
        return invokeMethodHandle.invoke(method, instance, arguments)
    }

    fun setPrivateVariable(fieldName: String, instanceToModify: Any, newValue: Any?) {
        var field: Any? = null
        try {
            field = instanceToModify.javaClass.getField(fieldName)
        } catch (e: Throwable) {
            try {
                field = instanceToModify.javaClass.getDeclaredField(fieldName)
            } catch (e: Throwable) {
            }
        }

        setFieldAccessibleHandle.invoke(field, true)
        setFieldHandle.invoke(field, instanceToModify, newValue)
    }

    //Required to get certain variables.
    private fun getPrivateVariable(fieldName: String, instanceToGetFrom: Any): Any? {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val getMethod = MethodHandles.lookup()
            .findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
        val getNameMethod =
            MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
        val setAcessMethod = MethodHandles.lookup().findVirtual(
            fieldClass,
            "setAccessible",
            MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType)
        )

        val instancesOfFields: Array<out Any> = instanceToGetFrom.javaClass.getDeclaredFields()
        for (obj in instancesOfFields) {
            setAcessMethod.invoke(obj, true)
            val name = getNameMethod.invoke(obj)
            if (name.toString() == fieldName) {
                return getMethod.invoke(obj, instanceToGetFrom)
            }
        }
        return null
    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private var getChildrenCopyHandle: Any? = null
    private fun UIPanelAPI.getChildrenCopy(): List<UIComponentAPI> {
        if (getChildrenCopyHandle == null) {
            getChildrenCopyHandle = getMethod("getChildrenCopy", this)
        }
        return invokeMethodRef(getChildrenCopyHandle, this) as List<UIComponentAPI>
    }

    //thanks lyravega
    fun syncVariantIfNeeded() {
        if (requiresVariantUpdate) {
            saveCurrentVariant()
            setFleetMember(null)
            syncWithCurrentVariant()
            setFleetMember(getMember())
            syncWithCurrentVariant()
            setEditedSinceLoad()
            setEditedSinceSave()
            requiresVariantUpdate = false
        }
    }

    private var refitGetMemberHandle: Any? = null
    private fun getMember(): FleetMemberAPI? {
        if (refitGetMemberHandle == null) {
            refitGetMemberHandle = getMethod("getMember", refitPanel!!)
        }
        return invokeMethodRef(refitGetMemberHandle, refitPanel!!) as FleetMemberAPI?
    }

    private var displaySetFleetMemberHandle: Any? = null
    private fun setFleetMember(member: FleetMemberAPI?) {
        if (displaySetFleetMemberHandle == null) {
            displaySetFleetMemberHandle = getMethod("setFleetMember", shipDisplay!!, FleetMemberAPI::class.java, ShipVariantAPI::class.java)
        }
        invokeMethodRef(displaySetFleetMemberHandle, shipDisplay!!, member, null)
    }

    private var refitSaveCurrentVariantHandle: Any? = null
    private fun saveCurrentVariant() {
        if (refitSaveCurrentVariantHandle == null) {
            refitSaveCurrentVariantHandle = getMethod("saveCurrentVariant", refitPanel!!)
        }
        invokeMethodRef(refitSaveCurrentVariantHandle, refitPanel!!)
    }

    private var refitSyncWithCurrentVariantHandle: Any? = null
    private fun syncWithCurrentVariant() {
        if (refitSyncWithCurrentVariantHandle == null) {
            refitSyncWithCurrentVariantHandle = getMethod("syncWithCurrentVariant", refitPanel!!)
        }
        invokeMethodRef(refitSyncWithCurrentVariantHandle, refitPanel!!)
    }

    private var refitSetEditedSinceLoadHandle: Any? = null
    private fun setEditedSinceLoad() {
        if (refitSetEditedSinceLoadHandle == null) {
            refitSetEditedSinceLoadHandle = getMethod("setEditedSinceLoad", refitPanel!!, Boolean::class.java)
        }
        invokeMethodRef(refitSetEditedSinceLoadHandle, refitPanel!!, false)
    }

    private var refitSetEditedSinceSaveHandle: Any? = null
    private fun setEditedSinceSave() {
        if (refitSetEditedSinceSaveHandle == null) {
            refitSetEditedSinceSaveHandle = getMethod("setEditedSinceSave", refitPanel!!, Boolean::class.java)
        }
        invokeMethod("setEditedSinceSave", refitPanel!!, false)
    }

    fun removeExoticaButton() {
        openButtonPanel?.let {
            modWidget!!.removeComponent(it)
        }

        closeButtonPanel?.let {
            modWidget!!.removeComponent(it)
        }
    }
}

fun FleetMemberAPI.checkRefitVariant(): ShipVariantAPI {
    if (RefitButtonAdder.member == this) {
        return RefitButtonAdder.variant as ShipVariantAPI
    }
    return this.variant
}