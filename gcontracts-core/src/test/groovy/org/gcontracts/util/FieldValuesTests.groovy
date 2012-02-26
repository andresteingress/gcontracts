package org.gcontracts.util

/**
 * @author me@andresteingress.com
 */
class FieldValuesTests extends GroovyTestCase {
    
    static class A {
        private int i = 12
    }
    
    static class B extends A {}
    
    void testPrivateFieldValueAccessFromBaseClass() {
        def b = new B()
        
        int value = FieldValues.fieldValue(b, "i", int)
        assertEquals value, 12
    }
}
