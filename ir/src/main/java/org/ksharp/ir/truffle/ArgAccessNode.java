package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ArgAccessNode extends KSharpNode {

    private final int index;

    public ArgAccessNode(int index) {
        this.index = index;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return frame.getArguments()[index];
    }
}
