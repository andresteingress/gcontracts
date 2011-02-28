package org.gcontracts.domain;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.util.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a contract between a supplier and a customer of a class.
 *
 * @author andre.steingress@gmail.com
 */
public class Contract {

    private final ClassNode classNode;

    private ClassInvariant classInvariant = ClassInvariant.DEFAULT;
    private final Map<MethodNode, Precondition> preconditionMap;
    private final Map<MethodNode, Postcondition> postconditionMap;

    public Contract(final ClassNode classNode)  {
        Validate.notNull(classNode);

        this.classNode = classNode;
        this.preconditionMap = new HashMap<MethodNode, Precondition>();
        this.postconditionMap = new HashMap<MethodNode, Postcondition>();
    }

    public ClassNode classNode() { return classNode; }

    public void setClassInvariant(final ClassInvariant classInvariant)  {
        Validate.notNull(classInvariant);
        this.classInvariant = classInvariant;
    }

    public void addPrecondition(final MethodNode methodNode, final Precondition precondition)  {
        Validate.notNull(methodNode);
        Validate.notNull(precondition);

        if (!preconditionMap.containsKey(methodNode))  {
            preconditionMap.put(methodNode, precondition);
        } else {
            preconditionMap.get(methodNode).and(precondition);
        }
    }

    public void addPostcondition(final MethodNode methodNode, final Postcondition postcondition)  {
        Validate.notNull(methodNode);
        Validate.notNull(postcondition);

        if (!postconditionMap.containsKey(methodNode))  {
            postconditionMap.put(methodNode, postcondition);
        } else {
            postconditionMap.get(methodNode).and(postcondition);
        }
    }

    public Map<MethodNode, Precondition> preconditions() { return preconditionMap; }
    public Map<MethodNode, Postcondition> postconditions() { return postconditionMap; }

    public boolean hasDefaultClassInvariant() { return classInvariant == ClassInvariant.DEFAULT; }
    public ClassInvariant classInvariant() { return classInvariant; }
}
