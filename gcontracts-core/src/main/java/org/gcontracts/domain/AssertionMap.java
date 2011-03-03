package org.gcontracts.domain;

import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.util.Validate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author andre.steingress@gmail.com
 */
public class AssertionMap<T extends Assertion<T>> implements Iterable<Map.Entry<MethodNode, T>> {

    private final Map<MethodNode, T> internalMap;

    public AssertionMap() {
        this.internalMap = new HashMap<MethodNode, T>();
    }

    public void and(final MethodNode methodNode, final T assertion)  {
        Validate.notNull(methodNode);
        Validate.notNull(assertion);

        if (!internalMap.containsKey(methodNode))  {
            internalMap.put(methodNode, assertion);
        } else {
            internalMap.get(methodNode).and(assertion);
        }
    }

    public void or(final MethodNode methodNode, final T assertion)  {
        Validate.notNull(methodNode);
        Validate.notNull(assertion);

        if (!internalMap.containsKey(methodNode))  {
            internalMap.put(methodNode, assertion);
        } else {
            internalMap.get(methodNode).or(assertion);
        }
    }

    public void join(final MethodNode methodNode, final T assertion)  {
        and(methodNode, assertion);
    }

    public boolean contains(final MethodNode methodNode)  {
        return internalMap.containsKey(methodNode);
    }

    public Iterator<Map.Entry<MethodNode, T>> iterator() {
        return internalMap.entrySet().iterator();
    }

    public int size()  {
        return internalMap.size();
    }

    public T get(final MethodNode methodNode)  {
        return internalMap.get(methodNode);
    }
}
