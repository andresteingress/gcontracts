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
        
        def value = FieldValues.fieldValue(b, "i")
        assertEquals value, 12
    }
}
