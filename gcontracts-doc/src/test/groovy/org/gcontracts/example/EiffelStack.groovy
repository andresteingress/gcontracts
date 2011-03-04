package org.gcontracts.example

import org.gcontracts.annotations.*

/**
 * This class should serve as an example for GContracts' customer groovydoc task.
 *
 * @author ast
 */
@Invariant({ elements != null })
class EiffelStack {

    private List elements

    @Ensures({ is_empty() })
    public EiffelStack()  {
        elements = []
    }

    @Requires({ preElements?.size() > 0 })
    @Ensures({ !is_empty() })
    public EiffelStack(List preElements)  {
        elements = preElements
    }

    def boolean is_empty()  {
        elements.isEmpty()
    }

    @Requires({ !is_empty() })
    def last_item()  {
        elements.last()
    }

    def count() {
        elements.size()
    }

    @Ensures({ result == true ? count() > 0 : count() >= 0  })
    def boolean has(def item)  {
        elements.contains(item)
    }

    @Ensures({ last_item() == item })
    def put(def item)  {
       elements.push(item)
    }

    @Requires({ !is_empty() })
    @Ensures({ last_item() == item })
    def replace(def item)  {
        remove()
        elements.push(item)
    }

    @Requires({ !is_empty() })
    @Ensures({ result != null })
    def remove()  {
        elements.pop()
    }
}
