package org.gcontracts.ast;

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.ASTTransformation;

import java.lang.reflect.Field;

/**
 * @author andre.steingress@gmail.com
 */
public abstract class BaseASTTransformation implements ASTTransformation {

    /**
     * Reads the protected <tt>source</tt> instance variable of {@link org.codehaus.groovy.control.SourceUnit}.
     *
     * @param unit the {@link org.codehaus.groovy.control.SourceUnit} to retrieve the {@link org.codehaus.groovy.control.io.ReaderSource} from
     * @return the {@link org.codehaus.groovy.control.io.ReaderSource} of the given <tt>unit</tt>.
     */
    protected ReaderSource getReaderSource(SourceUnit unit)  {

        try {
            Class sourceUnitClass = unit.getClass();

            while (sourceUnitClass != SourceUnit.class)  {
                sourceUnitClass = sourceUnitClass.getSuperclass();
            }

            Field field = sourceUnitClass.getDeclaredField("source");
            field.setAccessible(true);

            return (ReaderSource) field.get(unit);
        } catch (Exception e) {
            return null;
        }
    }

}
