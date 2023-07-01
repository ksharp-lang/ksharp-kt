package org.ksharp.ir.truffle.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import org.ksharp.ir.truffle.KSharpNode;

public class StringNode extends KSharpNode {

    private final TruffleString value;

    public StringNode(String value) {
        this.value = TruffleString.FromJavaStringNode.create().execute(value, TruffleString.Encoding.UTF_16);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return value;
    }

}
