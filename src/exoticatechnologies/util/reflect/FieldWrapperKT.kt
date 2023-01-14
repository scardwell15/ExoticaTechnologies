package exoticatechnologies.util.reflect

class FieldWrapperKT<F>(val obj: Any? = null, val fieldObj: Any, val fieldType: Class<F>) {
    var value: F
        get() = ReflectionUtil.valueField.invoke(fieldObj, obj) as F
        set(value) { ReflectionUtil.setField.invoke(fieldObj, obj, value) }
}