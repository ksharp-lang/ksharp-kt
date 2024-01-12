package org.ksharp.ir.truffle.bitwise;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public abstract class BaseBitwiseNode extends KSharpNode implements BitwiseOperations {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private BitwiseNode bitwiseNode;

    protected BaseBitwiseNode(KSharpNode left, KSharpNode right) {
        this.bitwiseNode = BitwiseNodeGen.create(left, right, this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return bitwiseNode.execute(frame);
    }

}
