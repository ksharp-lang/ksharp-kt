package org.ksharp.ir.truffle.call;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import kotlin.Pair;
import org.ksharp.ir.KValue;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.typesystem.types.Type;

public abstract class BaseCallNode extends KSharpNode {
    @Children
    private final KSharpNode[] arguments;
    private final TruffleString.ToJavaStringNode toJavaStringNode;
    protected final Type returnType;

    protected BaseCallNode(KSharpNode[] arguments, Type returnType) {
        this.arguments = arguments;
        this.returnType = returnType;
        toJavaStringNode = TruffleString.ToJavaStringNode.create();
    }

    public Pair<Type, Object[]> getArguments(VirtualFrame frame) {
        var argumentValues = new Object[arguments.length];
        Type firstArgumentType = null;
        for (int i = 0; i < arguments.length; i++) {
            var argument = arguments[i].execute(frame);
            if (argument instanceof TruffleString truffleStringArgument) {
                argument = toJavaStringNode.execute(truffleStringArgument);
            }
            if (i == 0) {
                firstArgumentType = KValue.type(argument);
            }
            argumentValues[i] = argument;
        }
        return new Pair<>(firstArgumentType, argumentValues);
    }


}
