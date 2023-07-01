package org.ksharp.ir.truffle.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

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
