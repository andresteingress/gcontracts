package org.gcontracts.compability

/**
 * User: asteingress
 * Date: 10/15/12
 */
class LockTests extends GroovyShellTestCase {

    void test_withReadAndWriteLock() {

        def result = evaluate """
        import groovy.transform.*;
        import org.gcontracts.annotations.*

         public class ResourceProvider {

             private final Map<String, String> data = new HashMap<String, String>();

            @Requires({ key })
            @WithReadLock
             public String getResource(String key) throws Exception {
                     return data.get(key);
             }

            @WithWriteLock
             public void refresh() throws Exception {
                data['test'] = 'test'
             }
         }

         def resourceProvider = new ResourceProvider()
         resourceProvider.refresh()

         resourceProvider.getResource('test')
        """

         assert result == 'test'
    }
}
