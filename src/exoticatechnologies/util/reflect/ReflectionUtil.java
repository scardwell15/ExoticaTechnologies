package exoticatechnologies.util.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectionUtil {
    static MethodHandle valueField;
    static MethodHandle nameField;
    static MethodHandle access;
    static MethodHandle setField;

    private static Object getClassField(Class clazz, String fieldName) {
        try {
            if (access == null) {
                Class methodClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
                valueField = MethodHandles.lookup().findVirtual(methodClass, "get", MethodType.methodType(Object.class, Object.class));
                nameField = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String.class));
                access = MethodHandles.lookup().findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
                setField = MethodHandles.lookup().findVirtual(methodClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
            }

            Object[] fields = clazz.getDeclaredFields();
            for (Object fieldObj : fields) {
                access.invoke(fieldObj, true);
                Object name = nameField.invoke(fieldObj);

                if (name.equals(fieldName)) {
                    return fieldObj;
                }
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    private static Object getObjectField(Object obj, String fieldName) {
        try {
            if (obj == null) return null;

            return getClassField(obj.getClass(), fieldName);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <F> FieldWrapperKT<F> getObjectFieldWrapper(Object obj, String fieldName, Class<F> type) {
        try {
            if (obj == null) return null;

            return new FieldWrapperKT<>(obj, getObjectField(obj, fieldName), type);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> FieldWrapper<T> getClassFieldWrapper(Class<T> clazz, String fieldName) {
        try {
            return new FieldWrapper<>(getObjectField(clazz, fieldName));
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

}
