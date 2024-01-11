package org.ksharp.ir.truffle.relational;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

import java.util.Objects;

public class NotEqualsNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode left;

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private KSharpNode right;

    public NotEqualsNode(KSharpNode left, KSharpNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return !Objects.equals(left.execute(frame), right.execute(frame));
    }
}
