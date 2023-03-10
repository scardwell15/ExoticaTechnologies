package exoticatechnologies.util.reflect

class MethodWrapperKT(val obj: Any? = null, val methodObj: Any) {
    fun invoke(vararg args: Any) {
        ReflectionUtil.invokeMethod.invoke(methodObj, obj, args)
    }
}