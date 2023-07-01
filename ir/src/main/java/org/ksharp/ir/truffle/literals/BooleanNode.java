package org.ksharp.ir.truffle.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public class BooleanNode extends KSharpNode {

    private final boolean value;

    public BooleanNode(boolean value) {
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return value;
    }

}
