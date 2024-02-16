package org.ksharp.ir.truffle.cast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import org.ksharp.ir.KValue;
import org.ksharp.ir.truffle.KSharpNode;

public class ToStringNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode expression;

    public ToStringNode(KSharpNode expression) {
        this.expression = expression;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        var value = String.valueOf(KValue.value(expression.execute(frame)));
        return TruffleString.FromJavaStringNode.create().execute(value, TruffleString.Encoding.UTF_16);
    }
}
