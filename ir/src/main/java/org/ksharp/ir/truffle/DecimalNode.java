package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class DecimalNode extends KSharpNode {

    private final double value;

    public DecimalNode(double value) {
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return value;
    }

}
