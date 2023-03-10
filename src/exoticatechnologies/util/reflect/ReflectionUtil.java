package exoticatechnologies.util.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectionUtil {
    static MethodHandle valueField;
    static MethodHandle nameField;
    static MethodHandle fieldAccess;
    static MethodHandle setField;

    static MethodHandle accessMethod;
    static MethodHandle invokeMethod;
    static MethodHandle nameMethod;

    private static Object getClassMethod(Class clazz, String fieldName, Class[] params) {
        try {
            if (accessMethod == null) {
                Class methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
                accessMethod = MethodHandles.lookup().findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
                invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
                nameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String.class));
            }

            Object[] methods = clazz.getMethods();
            for (Object methodObj : methods) {
                accessMethod.invoke(methodObj, true);
                Object name = nameMethod.invoke(methodObj);

                if (name.equals(fieldName)) {
                    return methodObj;
                }
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    private static Object getClassField(Class clazz, String fieldName) {
        try {
            if (fieldAccess == null) {
                Class methodClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
                valueField = MethodHandles.lookup().findVirtual(methodClass, "get", MethodType.methodType(Object.class, Object.class));
                nameField = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String.class));
                fieldAccess = MethodHandles.lookup().findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
                setField = MethodHandles.lookup().findVirtual(methodClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
            }

            Object[] fields = clazz.getDeclaredFields();
            for (Object fieldObj : fields) {
                fieldAccess.invoke(fieldObj, true);
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

    public static MethodWrapperKT getObjectMethodWrapper(Object obj, String methodName, Class... args) {
        try {
            return new MethodWrapperKT(obj, getClassMethod(obj.getClass(), methodName, args));
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
