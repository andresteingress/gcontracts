package org.gcontracts.util

/**
 * @author me@andresteingress.com
 */
class FieldValuesTests extends GroovyTestCase {
    
    static class A {
        private int i = 12

        def c = { i + 1 }
    }
    
    static class B extends A {}
    
    void testPrivateFieldValueAccessFromBaseClass() {
        def b = new B()

        int value = FieldValues.fieldValue(b.c, "i", int)
        assertEquals value, 12
    }
}
