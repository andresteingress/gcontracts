package org.gcontracts.generation

import org.junit.Test

/**
 * @author ast
 */
class ContractExecutionTrackerTests {

    @Test void track_double_execution() {

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre')
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre') == false

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre')

    }

    @Test void clear_only_for_first_stack_element() {

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre')
        assert ContractExecutionTracker.track('Dummy', 'method 2', 'pre')
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre') == false

        ContractExecutionTracker.clear('Dummy', 'method 2', 'pre')

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre') == false
        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre')
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre')

    }

}
