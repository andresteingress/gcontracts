package org.gcontracts.util;

import java.lang.reflect.Field;

/**
 * This utility is ment to be used to replace direct calls to private
 * field variables in class invariants.
 *
 * @author me@andresteingress.com
 */
public class FieldValues {

    @SuppressWarnings("unchecked")
    public static <T> T fieldValue(Object obj, String fieldName, Class<T> type) throws IllegalAccessException {
        Validate.notNull(obj);
        Validate.notNull(fieldName);

        Field f = ReflectionUtils.findField(obj.getClass(), "thisObject");
        if (f == null) throw new IllegalArgumentException("Field thisObject could not be found!");
        f.setAccessible(true);

        Object target = f.get(obj);

        f = ReflectionUtils.findField(target.getClass(), fieldName);
        if (f == null) throw new IllegalArgumentException("Field " + fieldName + " could not be found!");
        f.setAccessible(true);

        return (T) f.get(target);
    }
    
}
