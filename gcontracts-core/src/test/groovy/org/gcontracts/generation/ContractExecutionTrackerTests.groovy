package org.gcontracts.generation

import org.junit.Test

/**
 * @author ast
 */
class ContractExecutionTrackerTests {

    @Test void track_double_execution() {

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false) == false

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)
    }

    @Test void clear_only_for_first_stack_element() {

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 2', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false) == false

        ContractExecutionTracker.clear('Dummy', 'method 2', 'pre', false)

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false) == false
        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false)
    }

    @Test void track_static_method() {

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', true)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', true) == false

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', true)
    }

}
