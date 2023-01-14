package exoticatechnologies.util.reflect;

public class FieldWrapper<T> {
    T obj;
    Object field;

    FieldWrapper(Object field) {
        this(null, field);
    }

    FieldWrapper(T obj, Object field) {
        this.obj = obj;
        this.field = field;
    }

    public void setValue(Object value) {
        if (obj != null) {
            setValue(obj, value);
        }
    }

    public void setValue(T obj, Object value) {
        try {
            ReflectionUtil.setField.invoke(field, obj, value);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object getValue() {
        if (obj != null) {
            return getValue(obj);
        }
        return null;
    }

    public Object getValue(T obj) {
        try {
            return ReflectionUtil.valueField.invoke(field, obj);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
