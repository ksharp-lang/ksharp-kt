package org.ksharp.ir.truffle.bitwise;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.numeric.IntegerNode;
import org.ksharp.ir.truffle.numeric.IntegerNodeGen;
import org.ksharp.ir.truffle.numeric.IntegerOperations;

public abstract class BaseBitwiseNode extends KSharpNode implements IntegerOperations {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private IntegerNode integerNode;

    protected BaseBitwiseNode(KSharpNode left, KSharpNode right) {
        this.integerNode = IntegerNodeGen.create(left, right, this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return integerNode.execute(frame);
    }

}
