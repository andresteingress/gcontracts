package org.gcontracts.classgen.asm;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.Verifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Large parts have been backported from Groovy 1.8
 *
 * @author andre.steingress@gmail.com
 */
public class ClosureWriter {

    private int closureCount = 1;

    public ClassNode createClosureClass(ClassNode classNode, ClosureExpression expression, int mods) {
        ClassNode outerClass = getOutermostClass(classNode);
        String name = outerClass.getName() + "$" + getClosureInnerName(outerClass, classNode);

        ArrayList<Parameter> parametersTemp = new ArrayList<Parameter>(Arrays.asList(expression.getParameters()));
        removeParameter("old", parametersTemp);
        removeParameter("result", parametersTemp);

        parametersTemp.add(new Parameter(ClassHelper.DYNAMIC_TYPE, "result"));
        parametersTemp.add(new Parameter(ClassHelper.DYNAMIC_TYPE, "old"));

        Parameter[] parameters = parametersTemp.toArray(new Parameter[parametersTemp.size()]);

        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        } else if (parameters.length == 0) {
            // let's create a default 'it' parameter
            Parameter it = new Parameter(ClassHelper.OBJECT_TYPE, "it", ConstantExpression.NULL);
            parameters = new Parameter[]{it};
            Variable ref = expression.getVariableScope().getDeclaredVariable("it");
            if (ref!=null) it.setClosureSharedVariable(ref.isClosureSharedVariable());
        }

        Parameter[] localVariableParams = getClosureSharedVariables(expression);
        removeInitialValues(localVariableParams);

        InnerClassNode answer = new InnerClassNode(outerClass, name, mods, ClassHelper.CLOSURE_TYPE); // closures are local inners and not public
        answer.setEnclosingMethod(null);
        answer.setSynthetic(true);
        answer.setUsingGenerics(outerClass.isUsingGenerics());

        MethodNode method =
                answer.addMethod("doCall", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, parameters, ClassNode.EMPTY_ARRAY, expression.getCode());
        method.setSourcePosition(expression);

        VariableScope varScope = expression.getVariableScope();
        if (varScope == null) {
            throw new RuntimeException(
                    "Must have a VariableScope by now! for expression: " + expression + " class: " + name);
        } else {
            method.setVariableScope(varScope.copy());
        }
        if (parameters.length > 1
                || (parameters.length == 1
                && parameters[0].getType() != null
                && parameters[0].getType() != ClassHelper.OBJECT_TYPE)) {

            // let's add a typesafe call method
            MethodNode call = answer.addMethod(
                    "call",
                    ACC_PUBLIC,
                    ClassHelper.OBJECT_TYPE,
                    parameters,
                    ClassNode.EMPTY_ARRAY,
                    new ReturnStatement(
                            new MethodCallExpression(
                                    VariableExpression.THIS_EXPRESSION,
                                    "doCall",
                                    new ArgumentListExpression(parameters))));
            call.setSourcePosition(expression);
        }

        // let's make the constructor
        BlockStatement block = new BlockStatement();
        // this block does not get a source position, because we don't
        // want this synthetic constructor to show up in corbertura reports
        VariableExpression outer = new VariableExpression("_outerInstance");
        outer.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(outer);
        VariableExpression thisObject = new VariableExpression("_thisObject");
        thisObject.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(thisObject);
        TupleExpression conArgs = new TupleExpression(outer, thisObject);
        block.addStatement(
                new ExpressionStatement(
                        new ConstructorCallExpression(
                                ClassNode.SUPER,
                                conArgs)));

        // let's assign all the parameter fields from the outer context
        for (Parameter param : localVariableParams) {
            String paramName = param.getName();
            ClassNode type = param.getType();
            if (true) {
                VariableExpression initialValue = new VariableExpression(paramName);
                initialValue.setAccessedVariable(param);
                initialValue.setUseReferenceDirectly(true);
                ClassNode realType = type;
                type = ClassHelper.makeReference();
                param.setType(ClassHelper.makeReference());
                FieldNode paramField = answer.addField(paramName, ACC_PRIVATE | ACC_SYNTHETIC, type, initialValue);
                paramField.setDeclaringClass(ClassHelper.getWrapper(param.getOriginType()));
                paramField.setHolder(true);
                String methodName = Verifier.capitalize(paramName);

                // let's add a getter & setter
                Expression fieldExp = new FieldExpression(paramField);
                answer.addMethod(
                        "get" + methodName,
                        ACC_PUBLIC,
                        realType,
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        new ReturnStatement(fieldExp));
            }
        }

        Parameter[] params = new Parameter[2];
        params[0] = new Parameter(ClassHelper.OBJECT_TYPE, "_outerInstance");
        params[1] = new Parameter(ClassHelper.OBJECT_TYPE, "_thisObject");
        // System.arraycopy(localVariableParams, 0, params, 2, localVariableParams.length);

        ASTNode sn = answer.addConstructor(ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, block);
        sn.setSourcePosition(expression);

        correctAccessedVariable(answer,expression);

        return answer;
    }

    private void removeParameter(String name, List<Parameter> parameters)  {
        for (Iterator<Parameter> it = parameters.iterator(); it.hasNext();)  {
            if (it.next().getName().equals(name)) it.remove();
        }
    }

    private ClassNode getOutermostClass(ClassNode outermostClass) {
        while (outermostClass instanceof InnerClassNode) {
            outermostClass = outermostClass.getOuterClass();
        }

        return outermostClass;
    }

    private Parameter[] getClosureSharedVariables(ClosureExpression ce) {
        VariableScope scope = ce.getVariableScope();
        Parameter[] ret = new Parameter[scope.getReferencedLocalVariablesCount()];
        int index = 0;
        for (Iterator iter = scope.getReferencedLocalVariablesIterator(); iter.hasNext();) {
            Variable element = (org.codehaus.groovy.ast.Variable) iter.next();
            Parameter p = new Parameter(element.getType(), element.getName());
            p.setDeclaringClass(element.getOriginType());
            p.setClosureSharedVariable(element.isClosureSharedVariable());
            ret[index] = p;
            index++;
        }
        return ret;
    }

    /*
     * this method is called for local variables shared between scopes.
     * These variables must not have init values because these would
     * then in later steps be used to create multiple versions of the
     * same method, in this case the constructor. A closure should not
     * have more than one constructor!
     */
    private void removeInitialValues(Parameter[] params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].hasInitialExpression()) {
                Parameter p = new Parameter(params[i].getType(), params[i].getName());
                p.setDeclaringClass(p.getOriginType());
                params[i] = p;
            }
        }
    }

    private void correctAccessedVariable(final InnerClassNode closureClass, ClosureExpression ce) {
        CodeVisitorSupport visitor = new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expression) {
                Variable v = expression.getAccessedVariable();
                if (v==null) return;
                if (!(v instanceof FieldNode)) return;
                String name = expression.getName();
                FieldNode fn = closureClass.getDeclaredField(name);
                expression.setAccessedVariable(fn);
            }
        };
        visitor.visitClosureExpression(ce);
    }

    private String getClosureInnerName(ClassNode owner, ClassNode enclosingClass) {
        String ownerShortName = owner.getNameWithoutPackage();
        String classShortName = enclosingClass.getNameWithoutPackage();
        if (classShortName.equals(ownerShortName)) {
            classShortName = "";
        }
        else {
            classShortName += "_";
        }
        // remove $
        int dp = classShortName.lastIndexOf("$");
        if (dp >= 0) {
            classShortName = classShortName.substring(++dp);
        }
        // remove leading _
        if (classShortName.startsWith("_")) {
            classShortName = classShortName.substring(1);
        }

        return "_gc_" + classShortName + "closure" + closureCount++;
    }
}
