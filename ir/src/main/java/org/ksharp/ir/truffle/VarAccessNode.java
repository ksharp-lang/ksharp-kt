package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class VarAccessNode extends KSharpNode {

    private final int index;

    public VarAccessNode(int index) {
        this.index = index;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return frame.getObject(index);
    }
}
