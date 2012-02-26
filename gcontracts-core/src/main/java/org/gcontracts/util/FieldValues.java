package org.gcontracts.util;

import java.lang.reflect.Field;

/**
 * This utility is ment to be used to replace direct calls to private
 * field variables in class invariants.
 *
 * @author me@andresteingress.com
 */
public class FieldValues {

    public static Object fieldValue(Object obj, String fieldName) throws IllegalAccessException {
        Validate.notNull(obj);
        Validate.notNull(fieldName);
        
        Field f = ReflectionUtils.findField(obj.getClass(), fieldName);
        if (f == null) throw new IllegalArgumentException("Field " + fieldName + " could not be found!");
    
        f.setAccessible(true);
        return f.get(obj);
    }
    
}
