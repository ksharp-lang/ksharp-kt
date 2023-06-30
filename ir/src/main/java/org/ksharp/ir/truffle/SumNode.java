package org.ksharp.ir.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;

public class SumNode extends KSharpNode {
    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode left;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode right;

    public SumNode(KSharpNode left, KSharpNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        long l = (Long) left.execute(frame);
        long r = (Long) right.execute(frame);
        return l + r;
    }
}
