package org.gcontracts.tests.other

import org.codehaus.groovy.control.CompilationFailedException

/**
 * @author me@andresteingress.com
 */
class ClosureExpressionValidationTests extends GroovyShellTestCase {

    void testCheckMissingExpressionsClassInvariant() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
            import org.gcontracts.annotations.*

            @Invariant({})
            class A {}

            def a = new A()
            """
        }

        assertTrue msg.contains ("Annotation does not contain any expressions")
    }

    void testCheckMissingExpressionsPrecondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import org.gcontracts.annotations.*

                class A {
                   @Requires({})
                   def op() {}
                }

                def a = new A()
                """
        }

        assertTrue msg.contains ("Annotation does not contain any expressions")
    }

    void testCheckMissingExpressionsPostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                    import org.gcontracts.annotations.*

                    class A {
                       @Ensures({})
                       def op() {}
                    }

                    def a = new A()
                    """
        }

        assertTrue msg.contains ("Annotation does not contain any expressions")
    }

    void testParameterSpecifiedClassInvariant() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import org.gcontracts.annotations.*

                @Invariant({ some -> 1 == 1 })
                class A {}

                def a = new A()
                """
        }

        assertTrue msg.contains ("Annotation does not support parameters")
    }

    void testParameterSpecifiedPostcondition() {

        evaluate """
                import org.gcontracts.annotations.*

                class A {

                    @Ensures({ result })
                    def op() {}
                }

                def a = new A()
        """
    }

    void testParameterNamesPostcondition() {

        def msg = shouldFail  CompilationFailedException, {
            evaluate """
            import org.gcontracts.annotations.*

            class A {

                @Ensures({ test -> 1 == 1 })
                def op() {}
            }

            def a = new A()
        """
        }

        assertTrue msg.contains("Postconditions only allow 'old' and 'result' closure parameters")
    }

    void testParameterWithExplicitTypePostcondition() {

        def msg = shouldFail  CompilationFailedException, {
            evaluate """
                import org.gcontracts.annotations.*

                class A {

                    @Ensures({ java.util.Map<String, Object> result -> 1 == 1 })
                    def op() {}
                }

                def a = new A()
            """
        }

        assertTrue msg.contains("Postconditions do not support explicit types")
    }

    void testPrivateVariableAccess() {

        def msg = shouldFail  CompilationFailedException, {
            evaluate """
                    import org.gcontracts.annotations.*

                    class A {
                        private int i

                        @Requires({ i })
                        def op() {}
                    }

                    def a = new A()
                """
        }

        assertTrue msg.contains("Access to private fields is not allowed, except in class invariants.")
    }


    void testClosureItAccess() {

        def msg = shouldFail  CompilationFailedException, {
            evaluate """
                        import org.gcontracts.annotations.*

                        @Invariant({ it })
                        class A {

                        }

                        def a = new A()
                    """
        }

        assertTrue msg.contains("Access to 'it' is not supported.")
    }

    void testPrefixOperatorUsage() {

        def msg = shouldFail  CompilationFailedException, {
            evaluate """
                import org.gcontracts.annotations.*

                class A {

                    @Requires({ ++arg })
                    def op(def arg) {}
                }

                def a = new A()
            """
        }

        assertTrue msg.contains("State changing postfix & prefix operators are not supported.")
    }

    void testPostfixOperatorUsage() {

        def msg = shouldFail  CompilationFailedException, {
            evaluate """
                    import org.gcontracts.annotations.*

                    class A {

                        @Requires({ arg++ })
                        def op(def arg) {}
                    }

                    def a = new A()
                """
        }

        assertTrue msg.contains("State changing postfix & prefix operators are not supported.")
    }
}
