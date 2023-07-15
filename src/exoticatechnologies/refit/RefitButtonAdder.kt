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

    var buttonPanel: CustomPanelAPI? = null
    var member: FleetMemberAPI? = null
    var variant: HullVariantSpec? = null
    var firstButtonLoad = true

    companion object {
        var requiresVariantUpdate = false
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {

        //Returns if not paused, so that this doesnt run while the player isnt in any UI screen.
        if (!Global.getSector().isPaused)
        {
            return
        }

        if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.REFIT)
        {
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

                        var shipdisplay = invokeMethod("getShipDisplay", child3!!) as UIPanelAPI?
                        variant = invokeMethod("getCurrentVariant", shipdisplay!!) as HullVariantSpec?

                        if (requiresVariantUpdate)
                        {
                            try {
                                invokeMethod("syncWithCurrentVariant", child3!!, true)
                            } catch (e: Throwable) {
                                try {
                                    invokeMethod("syncWithCurrentVariant", child3!!)
                                } catch (e: Throwable) {
                                    var test = ""
                                }
                            }
                            requiresVariantUpdate = false
                        }

                        member = invokeMethod("getMember", child3) as FleetMemberAPI

                        var child4 = child3.getChildrenCopy().find { hasMethodOfName("getColorFor", it) } as UIPanelAPI?

                        if (child4 is UIPanelAPI)
                        {
                            modWidget = child4.getChildrenCopy().find { hasMethodOfName("removeNotApplicableMods", it) } as UIPanelAPI?


                            if (modWidget is UIPanelAPI)
                            {
                                if (modWidget.getChildrenCopy().any { it == buttonPanel }) {
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
            buttonPanel = Global.getSettings().createCustom(buildButton.position.width , buildButton.position.height, null)
            modWidget.addComponent(buttonPanel)
            buttonPanel!!.position.belowLeft(buildButton, 3f)

            var element = buttonPanel!!.createUIElement(buildButton.position.width , buildButton.position.height, false)
            element.position.inTL(-5f, 0f)
            buttonPanel!!.addUIElement(element)
            var openExoticaButton = element.addLunaSpriteElement("graphics/ui/exoticaButton.png", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, buildButton.position.width , buildButton.position.height).apply {
                enableTransparency = true

            }

            element.setParaFont(Fonts.ORBITRON_20AA)
            var para = element.addPara("Exotica", 0f, Color(200, 150, 255).setAlpha(0), Misc.getHighlightColor())

           /* element.setTitleFont(Fonts.INSIGNIA_LARGE)
            var para = element.addTitle("Exotica", Color(100, 0, 200))*/
            para.position.inTL(buildButton.position.width / 2 - para.computeTextWidth(para.text) / 2 + 5 ,2f)

            openExoticaButton.getSprite().alphaMult = 0.8f

            openExoticaButton.onHoverEnter {
                openExoticaButton.getSprite().alphaMult = 1f
                openExoticaButton.playSound("ui_button_mouseover", 1f, 1f)
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

                        para.setColor(Color(200, 150, 255).setAlpha(colorAlpha.toInt()))
                    }

                }
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
                    var plugin = ExoticaPanelPlugin(corePanel, member!!)

                    var width = CustomExoticaPanel.getWidth()
                    var height = CustomExoticaPanel.getHeight()
                    var exoticaPanel = Global.getSettings().createCustom(width, height, plugin)
                    plugin.panel = exoticaPanel
                    corePanel.addComponent(exoticaPanel)
                    exoticaPanel.position.inTL(Global.getSettings().screenWidth / 2 - width / 2, Global.getSettings().screenHeight / 2 - height / 2)

                    var custom = CustomExoticaPanel()
                    custom.init(exoticaPanel, plugin, width, height,  member!!, variant!!)
                }
            }
        }
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any) : Boolean {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))

        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.any { getNameMethod.invoke(it) == name }
    }

    //Required to execute obfuscated methods without referencing their obfuscated class name.
    private fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?) : Any?
    {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        var foundMethod: Any? = null
        for (method in instance::class.java.methods as Array<Any>)
        {
            if (getNameMethod.invoke(method) == methodName)
            {
                foundMethod = method
            }
        }

        return invokeMethod.invoke(foundMethod, instance, arguments)
    }

    fun setPrivateVariable(fieldName: String, instanceToModify: Any, newValue: Any?)
    {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val setMethod = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
        val getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
        val setAcessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

        val instancesOfFields: Array<out Any> = instanceToModify.javaClass.getDeclaredFields()
        for (obj in instancesOfFields)
        {
            setAcessMethod.invoke(obj, true)
            val name = getNameMethod.invoke(obj)
            if (name.toString() == fieldName)
            {
                setMethod.invoke(obj, instanceToModify, newValue)
            }
        }
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

}