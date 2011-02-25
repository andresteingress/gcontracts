package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.generation.Configurator;
import org.gcontracts.util.Validate;
import org.objectweb.asm.Opcodes;

/**
 * Makes some initialization in order to use the {@link Configurator} for determining
 * which assertions in what packages will be executed.
 *
 * @see Configurator
 *
 * @author andre.steingress@gmail.com
 */
public class ConfiguratorSetupVisitor extends BaseVisitor {

    public ConfiguratorSetupVisitor(SourceUnit sourceUnit, ReaderSource source) {
        super(sourceUnit, source);
    }

    protected ConfiguratorSetupVisitor() {
        super();
    }

    @Override
    public void visitClass(ClassNode node) {
        addConfigurationVariable(node);
    }

    /**
     * Adds an instance field which allows to control whether GContract assertions
     * are enabled or not. Before assertions are evaluated this field will be checked.
     *
     * @see Configurator
     *
     * @param type the current {@link ClassNode}
     */
    protected void addConfigurationVariable(final ClassNode type) {
        Validate.notNull(type);

        MethodCallExpression methodCall = new MethodCallExpression(new ClassExpression(ClassHelper.makeWithoutCaching(Configurator.class)), "checkAssertionsEnabled", new ArgumentListExpression(new ConstantExpression(type.getName())));
        final FieldNode fieldNode = type.addField(GCONTRACTS_ENABLED_VAR, Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, ClassHelper.Boolean_TYPE, methodCall);
        fieldNode.setSynthetic(true);
    }
}
