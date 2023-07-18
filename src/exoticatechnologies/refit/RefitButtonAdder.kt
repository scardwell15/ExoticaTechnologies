package exoticatechnologies.refit

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
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

    var frames = 0

    var refitPanel: UIPanelAPI? = null
    var openButtonPanel: CustomPanelAPI? = null
    var closeButtonPanel: CustomPanelAPI? = null
    var member: FleetMemberAPI? = null
    var variant: HullVariantSpec? = null
    var firstButtonLoad = true
    var buttonAdded = false

    companion object {
        var requiresVariantUpdate = false
    }

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

    private val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    private val getMethodNameHandle = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    private val invokeMethodHandle = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {

        if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.REFIT)
        {
            buttonAdded = false
            return
        }

        if (buttonAdded) {
            member = invokeMethod("getMember", refitPanel!!) as FleetMemberAPI
            var shipdisplay = invokeMethod("getShipDisplay", refitPanel!!) as UIPanelAPI?
            variant = invokeMethod("getCurrentVariant", shipdisplay!!) as HullVariantSpec?
            return
        }

        var state = AppDriver.getInstance().currentState

        //Makes sure that the current state is the campaign state.
        if (state !is CampaignState) return

        var modWidget: UIPanelAPI? = null
        var buildButton: UIComponentAPI? = null

        var core = invokeMethod("getCore", state)

        var dialog = invokeMethod("getEncounterDialog", state)
        if (dialog != null)
        {
            core = invokeMethod("getCoreUI", dialog)
        }

        if (core is UIPanelAPI)
        {


            var child1 = core.getChildrenCopy().find { hasMethodOfName("setBorderInsetLeft", it) }
            if (child1 is UIPanelAPI)
            {
                var child2 = child1.getChildrenCopy().find { hasMethodOfName("goBackToParentIfNeeded", it) }

                if (child2 is UIPanelAPI)
                {
                    var child3 = child2.getChildrenCopy().find { hasMethodOfName("syncWithCurrentVariant", it) } as UIPanelAPI?

                    if (child3 is UIPanelAPI)
                    {
                        refitPanel = child3
                        member = invokeMethod("getMember", refitPanel!!) as FleetMemberAPI
                        var shipdisplay = invokeMethod("getShipDisplay", refitPanel!!) as UIPanelAPI?
                        variant = invokeMethod("getCurrentVariant", shipdisplay!!) as HullVariantSpec?

                        var child4 = child3.getChildrenCopy().find { hasMethodOfName("getColorFor", it) } as UIPanelAPI?

                        if (child4 is UIPanelAPI)
                        {
                            modWidget = child4.getChildrenCopy().find { hasMethodOfName("removeNotApplicableMods", it) } as UIPanelAPI?


                            if (modWidget is UIPanelAPI)
                            {
                                if (modWidget.getChildrenCopy().any { it == openButtonPanel }) {
                                    return
                                }

                                var buttons = modWidget.getChildrenCopy().filter { hasMethodOfName("getText", it) }

                                buildButton = buttons.find { (invokeMethod("getText", it) as String).contains("Build in") } as UIComponentAPI
                            }
                        }
                    }
                }
            }
        }
        if (modWidget == null)
        {
            firstButtonLoad = true
        }

        if (modWidget != null && buildButton != null)
        {
            buttonAdded = true

            openButtonPanel = Global.getSettings().createCustom(buildButton.position.width , buildButton.position.height, null)
            modWidget.addComponent(openButtonPanel)
            openButtonPanel!!.position.belowLeft(buildButton, 3f)

            var openElement = openButtonPanel!!.createUIElement(buildButton.position.width , buildButton.position.height, false)
            var exoticaButtonPos = openElement.position.inTL(-5f, 0f)
            openButtonPanel!!.addUIElement(openElement)
            var openExoticaButton = openElement.addLunaSpriteElement("graphics/ui/exoticaButton.png", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, buildButton.position.width , buildButton.position.height).apply {
                enableTransparency = true

            }

            openElement.setParaFont(Fonts.ORBITRON_20AA)
            var openPara = openElement.addPara("Exotica", 0f, Color(215, 175, 255).setAlpha(0), Misc.getHighlightColor())

            openPara.position.inTL(buildButton.position.width / 2 - openPara.computeTextWidth(openPara.text) / 2 + 5 ,2f)

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

                if (corePanel is UIPanelAPI)
                {
                    var plugin = ExoticaPanelPlugin(corePanel, member!!, this)

                    var width = CustomExoticaPanel.getWidth()
                    var height = CustomExoticaPanel.getHeight()
                    var exoticaPanel = Global.getSettings().createCustom(width, height, plugin)
                    plugin.panel = exoticaPanel
                    corePanel.addComponent(exoticaPanel)

                    var custom = CustomExoticaPanel()
                    custom.init(exoticaPanel, plugin, width, height,  member!!, variant!!)

                    exoticaPanel.position.inTL(exoticaButtonPos.x - 4f - width, Global.getSettings().screenHeight / 2 - height / 2)

                    closeButtonPanel = Global.getSettings().createCustom(buildButton.position.width , buildButton.position.height, null)
                    plugin.closeButtonPanel = closeButtonPanel
                    exoticaPanel.addComponent(closeButtonPanel)
                    //turns out hierarchical siblings can be anchored on if you remember to remove them from their parent panel before removing the parent panel.
                    closeButtonPanel!!.position.belowLeft(buildButton, 3f).setXAlignOffset(-4f)

                    var closeElement = closeButtonPanel!!.createUIElement(buildButton.position.width , buildButton.position.height, false)
                    closeButtonPanel!!.addUIElement(closeElement)
                    var closeExoticaButton = closeElement.addLunaSpriteElement("graphics/ui/exoticaButton.png", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, buildButton.position.width , buildButton.position.height).apply {
                        enableTransparency = true
                    }

                    closeElement.setParaFont(Fonts.ORBITRON_20AA)
                    var closePara = closeElement.addPara("Close", 0f, Color(215, 175, 255), Misc.getHighlightColor())

                    closePara.position.inTL(buildButton.position.width / 2 - closePara.computeTextWidth(closePara.text) / 2 + 5 ,2f)

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

            if (firstButtonLoad)
            {
                openExoticaButton.getSprite().alphaMult = 0.0f
                openExoticaButton.advance {
                    var sprite = openExoticaButton.getSprite()
                    if (sprite.alphaMult < 0.8f)
                    {
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
    private fun hasMethodOfName(name: String, instance: Any) : Boolean {

        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
    }

    //Required to execute obfuscated methods without referencing their obfuscated class name.
    fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?) : Any?
    {
        var method: Any? = null

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        method = clazz.getMethod(methodName, *methodType.parameterArray())

        return invokeMethodHandle.invoke(method, instance, arguments)
    }

    fun setPrivateVariable(fieldName: String, instanceToModify: Any, newValue: Any?)
    {
        var field: Any? = null
        try {  field = instanceToModify.javaClass.getField(fieldName) } catch (e: Throwable) {
            try {  field = instanceToModify.javaClass.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }

        setFieldAccessibleHandle.invoke(field, true)
        setFieldHandle.invoke(field, instanceToModify, newValue)
    }

    //Required to get certain variables.
    private fun getPrivateVariable(fieldName: String, instanceToGetFrom: Any): Any? {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val getMethod = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
        val getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
        val setAcessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

        val instancesOfFields: Array<out Any> = instanceToGetFrom.javaClass.getDeclaredFields()
        for (obj in instancesOfFields)
        {
            setAcessMethod.invoke(obj, true)
            val name = getNameMethod.invoke(obj)
            if (name.toString() == fieldName)
            {
                return getMethod.invoke(obj, instanceToGetFrom)
            }
        }
        return null
    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private fun UIPanelAPI.getChildrenCopy() : List<UIComponentAPI> {
        return invokeMethod("getChildrenCopy", this) as List<UIComponentAPI>
    }

    fun syncVariantIfNeeded() {
        if (requiresVariantUpdate)
        {
            try {
                invokeMethod("syncWithCurrentVariant", refitPanel!!, true)
            } catch (e: Throwable) {
                //do nothing
                try {
                    invokeMethod("syncWithCurrentVariant", refitPanel!!)
                } catch (e: Throwable) {
                    println("error while pre-syncing variant in refit: $e")
                }
            }

            try {
                invokeMethod("saveCurrentVariant", refitPanel!!, false)
            } catch (e: Throwable) {
                //do nothing
                try {
                    invokeMethod("saveCurrentVariant", refitPanel!!)
                } catch (e: Throwable) {
                    println("error while saving variant in refit: $e")
                }
            }

            try {
                invokeMethod("setEditedSinceSave", refitPanel!!, false)
            } catch (e: Throwable) {
                //do nothing
            }

            try {
                invokeMethod("syncWithCurrentVariant", refitPanel!!, true)
            } catch (e: Throwable) {
                try {
                    invokeMethod("syncWithCurrentVariant", refitPanel!!)
                } catch (e: Throwable) {
                    println("error while post-syncing variant in refit: $e")
                }
            }

            requiresVariantUpdate = false
        }

    }
}