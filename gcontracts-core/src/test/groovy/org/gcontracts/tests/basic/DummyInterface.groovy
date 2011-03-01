package org.gcontracts.tests.basic

import org.gcontracts.annotations.Requires

/**
 * @author andre.steingress@gmail.com
 */
public interface DummyInterface {

    @Requires({ param1 > 0 })
    def add(def param1)
}