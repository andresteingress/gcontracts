/**
 * Copyright (c) 2010, andre.steingress@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gcontracts.ast;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Custom AST transformation that removes closure annotations of {@link org.gcontracts.annotations.Invariant},
 * {@link org.gcontracts.annotations.Requires} and {@link org.gcontracts.annotations.Ensures} and adds Java
 * assertions which executing the constraint-code instead.
 * </p>
 * <p>
 * Whenever a constraint is broken an {@link AssertionError} will be thrown.
 * </p>
 *
 * @see AssertionError
 *
 * @author andre.steingress@gmail.com
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ContractValidationASTTransformation implements ASTTransformation {

    /**
     * {@link org.codehaus.groovy.transform.ASTTransformation#visit(org.codehaus.groovy.ast.ASTNode[], org.codehaus.groovy.control.SourceUnit)}
     */
    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = (ModuleNode)nodes[0];

        for (final ClassNode classNode : moduleNode.getClasses())  {
            new AssertionInjector(classNode).rewrite();
        }
    }
}

/**
 * Injects standard Java assertion statements which check the specified {@link org.gcontracts.annotations.Requires},
 * {@link org.gcontracts.annotations.Ensures} and {@link org.gcontracts.annotations.Invariant} closure annotations.
 *
 * @see org.gcontracts.annotations.Requires
 * @see org.gcontracts.annotations.Ensures
 * @see org.gcontracts.annotations.Invariant
 */
class AssertionInjector {

    private static final String CLOSURE_ATTRIBUTE_NAME = "value";

    private final ClassNode classNode;
    private ClosureExpression classInvariant;
    private FieldNode fieldInvariant;

    public AssertionInjector(ClassNode classNode) {
        this.classNode = classNode;
    }

    /**
     * Rewrites the current {@link org.codehaus.groovy.ast.ClassNode} and adds assertions for all supported
     * assertion types.
     */
    public void rewrite() {
        new ClassCodeVisitorSupport() {

            @Override
            public void visitClass(ClassNode type) {

                List<AnnotationNode> annotations = type.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getTypeClass() == Invariant.class)  {
                        generateInvariantAssertionStatement(type, annotation);
                    }
                }

                super.visitClass(type);
            }

            @Override
            public void visitMethod(MethodNode method) {

                super.visitMethod(method);

                List<AnnotationNode> annotations = method.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getTypeClass() == Requires.class)  {
                        generatePreconditionAssertionStatement(method, annotation);
                    } else if (annotation.getClassNode().getTypeClass() == Ensures.class)  {
                        generatePostconditionAssertionStatement(method, annotation);
                    }
                }

                // If there is a class invariant we will append the check to this invariant
                // after each method call
                if (classInvariant != null)  {
                    generateInvariantAssertionStatement(method);
                }
            }

            protected SourceUnit getSourceUnit() {
                return null;
            }
        }.visitClass(classNode);
    }

    /**
     * Reads the {@link org.gcontracts.annotations.Invariant} closure expression and adds a class-invariant to
     * all declard contructors of that <tt>type</tt>.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param annotation the {@link org.gcontracts.annotations.Invariant} annotation node
     */
    public void generateInvariantAssertionStatement(ClassNode type, AnnotationNode annotation)  {

        // get the closure annotation
        classInvariant = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);

        // adding super-calls to invariants of parent classes
        addCallsToSuperClassInvariants(type, classInvariant);

        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        // add a local protected field with the invariant closure - this is needed for invariant checks in inheritance lines
        fieldInvariant = type.addField("$invariant$" + type.getNameWithoutPackage(), Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.CLOSURE_TYPE, classInvariant);

        final BlockStatement assertionBlock = new BlockStatement();
        assertionBlock.addStatement(new AssertStatement(new BooleanExpression(
                new MethodCallExpression(new FieldExpression(fieldInvariant), "call", ArgumentListExpression.EMPTY_ARGUMENTS)
        ), new ConstantExpression("[invariant] " + type.getName())));

        for (ConstructorNode constructor : type.getDeclaredConstructors())  {
            ((BlockStatement) constructor.getCode()).addStatement(assertionBlock);
        }
    }

    /**
     * Modifies the given <tt>closure</tt> which contains that current class-invariant and adds a super-call the
     * the class-invariant of the next parent class which has the Invarian annotation.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param closure the current class-invariant as {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     */
    public void addCallsToSuperClassInvariants(ClassNode type, ClosureExpression closure)  {

        final ClassNode nextClassWithInvariant = AnnotationUtils.getNextClassNodeWithAnnotation(type.getSuperClass(), Invariant.class);
        if (nextClassWithInvariant == null) return;

        final String fieldName = "$invariant$" + nextClassWithInvariant.getNameWithoutPackage();
        FieldNode nextClassInvariantField = nextClassWithInvariant.getField(fieldName);
        if (nextClassInvariantField == null)  {
            nextClassInvariantField = new FieldNode(fieldName, Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.CLOSURE_TYPE, nextClassWithInvariant, null);
        }

        final BlockStatement blockStatement = (BlockStatement) closure.getCode();
        final ExpressionStatement expressionStatement = (ExpressionStatement) blockStatement.getStatements().get(0);

        final Expression expression = expressionStatement.getExpression();

        expressionStatement.setExpression(
                 new BinaryExpression(
                         new BooleanExpression(expression),
                         Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                         new BooleanExpression(new MethodCallExpression(new PropertyExpression(VariableExpression.THIS_EXPRESSION, fieldName), "call", ArgumentListExpression.EMPTY_ARGUMENTS))));
    }

    /**
     * Adds the current class-invariant to the given <tt>method</tt>.
     *
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     */
    public void generateInvariantAssertionStatement(MethodNode method)  {

        final BlockStatement assertionBlock = new BlockStatement();
        assertionBlock.addStatement(new AssertStatement(new BooleanExpression(
                new MethodCallExpression(new FieldExpression(fieldInvariant), "call", ArgumentListExpression.EMPTY_ARGUMENTS)
        ), new ConstantExpression("[invariant] " + method.getDeclaringClass().getName())));

        final Statement statement = method.getCode();
        if (statement instanceof BlockStatement)  {
            ((BlockStatement) statement).addStatement(assertionBlock);
        } else  {
            assertionBlock.getStatements().add(0, statement);
            method.setCode(assertionBlock);
        }
    }

    /**
     * Injects a precondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link org.gcontracts.annotations.Requires}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param annotation the {@link org.gcontracts.annotations.Requires} annotation
     */
    public void generatePreconditionAssertionStatement(MethodNode method, AnnotationNode annotation)  {

        // get the closure annotation
        final ClosureExpression closureExpression = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        final BlockStatement preconditionCheck = createAssertionExpression(method, closureExpression, "precondition");
        preconditionCheck.addStatement(method.getCode());

        method.setCode(preconditionCheck);
    }

    /**
     * Injects a postcondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link org.gcontracts.annotations.Ensures}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param annotation the {@link org.gcontracts.annotations.Ensures} annotation
     */
    public void generatePostconditionAssertionStatement(MethodNode method, AnnotationNode annotation)  {

        // get the closure annotation
        final ClosureExpression closureExpression = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        // check whether the closure uses a result or old variable
        boolean usesResultVariable = false;
        boolean usesOldVariable = false;
        boolean usesResultVariableFirst = false;

        for (Parameter closureParameter : closureExpression.getParameters())  {
            if (closureParameter.getName().equals("old"))  {
                usesOldVariable = true;
            } else if (closureParameter.getName().equals("result"))  {
                usesResultVariable = true;
                usesResultVariableFirst = !usesOldVariable;
            }
        }

        MapExpression oldVariableMap = new MapExpression();

        if (usesOldVariable)  oldVariableMap = new VariableGenerator().generateOldVariablesMap(method);

        final BlockStatement methodBlock = (BlockStatement) method.getCode();

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        final List<Statement> statements = methodBlock.getStatements();
        if (statements.size() > 0)  {
            BlockStatement postconditionCheck = null;

            if (method.getReturnType() != ClassHelper.VOID_TYPE && usesResultVariable)  {
                Statement lastStatement = statements.get(statements.size() - 1);

                ReturnStatement returnStatement = getReturnStatement(lastStatement);

                // Assign the return statement expression to a local variable of type Object
                VariableExpression resultVariable = new VariableExpression("result");
                ExpressionStatement resultVariableStatement = new ExpressionStatement(
                new DeclarationExpression(resultVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        returnStatement.getExpression()));

                statements.remove(statements.size() - 1);

                if (usesOldVariable && usesResultVariableFirst)  {
                    postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition", new VariableExpression(new Parameter(ClassHelper.DYNAMIC_TYPE, "result")), oldVariableMap);
                } else if (usesOldVariable && !usesResultVariableFirst)  {
                    postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition", oldVariableMap, new VariableExpression(new Parameter(ClassHelper.DYNAMIC_TYPE, "result")));    
                } else {
                    postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition", new VariableExpression(new Parameter(ClassHelper.DYNAMIC_TYPE, "result")));
                }

                postconditionCheck.getStatements().add(0, resultVariableStatement);

                methodBlock.addStatement(postconditionCheck);
                methodBlock.addStatement(returnStatement);
            } else {

                if (usesOldVariable)  {
                    postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition",  oldVariableMap);
                } else {
                    postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition");
                }

                methodBlock.addStatement(postconditionCheck);
            }
        }
    }

    /**
     * Gets a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} from the given {@link org.codehaus.groovy.ast.stmt.Statement}.
     *
     * @param lastStatement the last {@link org.codehaus.groovy.ast.stmt.Statement} of some method code block
     * @return a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} or <tt>null</tt>
     */
    private ReturnStatement getReturnStatement(Statement lastStatement)  {

        if (lastStatement instanceof ReturnStatement)  {
            return (ReturnStatement) lastStatement;
        } else if (lastStatement instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) lastStatement;
            List<Statement> statements = blockStatement.getStatements();

            return statements.size() > 0 ? getReturnStatement(statements.get(statements.size() - 1)) : null;
        } else {
            // the last statement in a Groovy method could also be an expression which result is treated as return value
            ExpressionStatement expressionStatement = (ExpressionStatement) lastStatement;
            return new ReturnStatement(expressionStatement);
        }
    }

    /**
     * Reusable method for creating assert statements for the given <tt>closureExpression</tt>, injected in the
     * given <tt>method</tt> and with optional closure parameters.
     *
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     * @param constraint the name of the constraint, used for assertion messages
     * @param optionalParameters expressions to be used as closure parameters
     *
     * @return a new {@link org.codehaus.groovy.ast.stmt.BlockStatement} which holds the assertion
     */
    private BlockStatement createAssertionExpression(MethodNode method, ClosureExpression closureExpression, String constraint, Expression... optionalParameters) {
        final BlockStatement assertionBlock = new BlockStatement();
        // assign the closure to a local variable and call() it
        final VariableExpression closureVariable = new VariableExpression("$" + constraint + "Closure");

        // create a local variable to hold a reference to the newly instantiated closure
        assertionBlock.addStatement(new ExpressionStatement(
                new DeclarationExpression(closureVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        closureExpression)));

        final List<Expression> expressions = new ArrayList<Expression>(Arrays.asList(optionalParameters));

        assertionBlock.addStatement(new AssertStatement(new BooleanExpression(
                new MethodCallExpression(closureVariable, "call", new ArgumentListExpression(expressions))
        ), new ConstantExpression("[" + constraint + "] method " + method.getName() + "(" + getMethodParameters(method) + ")")));

        return assertionBlock;
    }

    /**
     * Creates a representative {@link String} of the given {@link org.codehaus.groovy.ast.MethodNode}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to create the representation
     * @return a {@link String} representation of the given <tt>method</tt>
     */
    private String getMethodParameters(MethodNode method)  {
        final StringBuilder builder = new StringBuilder();

        for (Parameter parameter : method.getParameters())  {
            if (builder.length() > 0)  {
                builder.append(", ");
            }
            builder.append(parameter.getName()).append(":").append(parameter.getType().getTypeClass().getName());
        }

        return builder.toString();
    }
}

class VariableGenerator {

    /**
     * Each field of the class is assigned to an old variable before method execution. After method execution
     * a map with the old variable values is generated, which is than used as parameter in the {@link org.gcontracts.annotations.Ensures}
     * closure.
     *
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @return a {@link org.codehaus.groovy.ast.expr.MapExpression} which holds either none or some old variables
     */
    public MapExpression generateOldVariablesMap(MethodNode method) {

        final ClassNode declaringClass = method.getDeclaringClass();
        final ArrayList<ExpressionStatement> oldVariableAssignments = new ArrayList<ExpressionStatement>();
        final ArrayList<VariableExpression> oldVariableExpressions = new ArrayList<VariableExpression>();

        // create variable assignments for old variables
        for (final FieldNode fieldNode : declaringClass.getFields())   {
            final ClassNode fieldType = fieldNode.getType();

            if (fieldType.getName().startsWith("java.lang") || ClassHelper.isPrimitiveType(fieldType) || fieldType.getName().startsWith("java.math") ||
                    fieldType.getName().startsWith("java.util") || fieldType.getName().startsWith("java.sql"))  {

                MethodNode cloneMethod = fieldType.getMethod("clone", Parameter.EMPTY_ARRAY);
                // if a clone method is available, the value is cloned
                if (cloneMethod != null && fieldType.implementsInterface(ClassHelper.make("java.lang.Cloneable")))  {
                    VariableExpression oldVariable = new VariableExpression("$old$" + fieldNode.getName());

                    final MethodCallExpression methodCall = new MethodCallExpression(new FieldExpression(fieldNode), "clone", ArgumentListExpression.EMPTY_ARGUMENTS);
                    // return null if field is null
                    methodCall.setSafe(true);

                    ExpressionStatement oldVariableAssignment = new ExpressionStatement(
                        new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                                methodCall));

                    oldVariableExpressions.add(oldVariable);
                    oldVariableAssignments.add(oldVariableAssignment);

                } else if (ClassHelper.isPrimitiveType(fieldType) || fieldType.getName().startsWith("java.lang")) {
                    VariableExpression oldVariable = new VariableExpression("$old$" + fieldNode.getName());
                    ExpressionStatement oldVariableAssignment = new ExpressionStatement(
                        new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new FieldExpression(fieldNode)));

                    oldVariableExpressions.add(oldVariable);
                    oldVariableAssignments.add(oldVariableAssignment);
                }
            }
        }

        BlockStatement methodBlock = (BlockStatement) method.getCode();

        MapExpression oldVariablesMap = new MapExpression();
        // add old variable statements
        for (int i = 0; i < oldVariableAssignments.size(); i++)  {
            VariableExpression variable = oldVariableExpressions.get(i);

            methodBlock.getStatements().add(0, oldVariableAssignments.get(i));
            oldVariablesMap.addMapEntryExpression(new MapEntryExpression(new ConstantExpression(variable.getName().substring("$old$".length())), variable));
        }

        return oldVariablesMap;
    }
}
