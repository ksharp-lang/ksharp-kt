package org.ksharp.ir.truffle.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public class IntegerNode extends KSharpNode {
    private final long value;

    public IntegerNode(long value) {
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return value;
    }
}
