package org.gcontracts.generation

import org.junit.Test

/**
 * @author ast
 */
class ContractExecutionTrackerTests {

    @Test void track_double_execution() {

        assert ContractExecutionTracker.track('Dummy', 'method 1')
        assert ContractExecutionTracker.track('Dummy', 'method 1') == false

        ContractExecutionTracker.clear()

        assert ContractExecutionTracker.track('Dummy', 'method 1')

    }

}
