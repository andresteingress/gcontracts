package org.gcontracts.generation

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

 /**
 * @author ast
 */
class CircularMethodCallAwareMetaClassTests extends BaseTestClass {

    class Dummy {
        boolean called = false
        def some_method() {
            if (called) return
            called = true
            another_method()
        }

        def another_method()  {
            some_method()
        }
    }

    @Test void get_shared_instance_in_current_thread()  {
        def proxy = CircularMethodCallAwareMetaClass.getProxy(new Dummy())
        assert proxy == CircularMethodCallAwareMetaClass.getProxy(new Dummy())
    }

    @Test void get_new_shared_instance_on_release()  {
        def proxy = CircularMethodCallAwareMetaClass.getProxy(new Dummy())
        proxy.release()
        assert proxy != CircularMethodCallAwareMetaClass.getProxy(new Dummy())
    }

    @Test void detect_cyclic_method_call_in_Dummy_class()  {
        CircularMethodCallAwareMetaClass.getProxy(new Dummy())

        def dummy = new Dummy()
        shouldFail org.gcontracts.CircularAssertionCallException, {
            dummy.some_method()
        }
    }
}
