package org.gcontracts.util;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * @author andre.steingress@gmail.com
 */
public class ClosureToSourceConverter {

    /**
     * Converts a {@link org.codehaus.groovy.ast.expr.ClosureExpression} into a String source.
     *
     * @param closureExpression the {@link org.codehaus.groovy.ast.expr.ClosureExpression} for retrieving the source-code from
     * @param source the {@link org.codehaus.groovy.control.io.ReaderSource} if the current source unit
     * @return the source the closure was created from
     */
    public static String convert(ClosureExpression closureExpression, ReaderSource source) {

        if (source == null) {
            return "";
        }

        final int lineNumberStart = closureExpression.getLineNumber();
        final int lineNumberEnd   = closureExpression.getLastLineNumber();

        final StringBuilder builder = new StringBuilder();

        for (int i = lineNumberStart; i <= lineNumberEnd; i++)  {
            String line = source.getLine(i, null);
            if (line == null) return "";

            if (i == lineNumberStart && i != lineNumberEnd)  {
                builder.append(line.substring(closureExpression.getColumnNumber() - 1));
            } else if (i == lineNumberStart && i == lineNumberEnd)  {
                builder.append(line.substring(closureExpression.getColumnNumber() - 1, closureExpression.getLastColumnNumber() - 1));
            } else if (i == lineNumberEnd)  {
                builder.append(line.substring(0, closureExpression.getLastColumnNumber() - 1));
            } else {
                builder.append(line);
            }

            builder.append('\n');
        }

        String closureSource = builder.toString().trim();
        if (!closureSource.startsWith("{")) return "";

        return closureSource;
     }
}
