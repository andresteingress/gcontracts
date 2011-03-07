package org.gcontracts.generation

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

 /**
 * @author andre.steingress@gmail.com
 */
class CyclicMethodCallAwareMetaClassTests extends BaseTestClass {

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
        def proxy = CyclicMethodCallAwareMetaClass.getProxy(new Dummy())
        assert proxy == CyclicMethodCallAwareMetaClass.getProxy(new Dummy())
    }

    @Test void get_new_shared_instance_on_release()  {
        def proxy = CyclicMethodCallAwareMetaClass.getProxy(new Dummy())
        proxy.release()
        assert proxy != CyclicMethodCallAwareMetaClass.getProxy(new Dummy())
    }

    @Test void detect_cyclic_method_call_in_Dummy_class()  {
        CyclicMethodCallAwareMetaClass.getProxy(new Dummy())

        def dummy = new Dummy()
        shouldFail org.gcontracts.CyclicAssertionCallException, {
            dummy.some_method()
        }
    }
}
