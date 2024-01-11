package org.ksharp.ir.truffle.relational;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.ksharp.ir.truffle.KSharpNode;

public abstract class BaseRelationalNode extends KSharpNode implements RelationalOperations {

    @SuppressWarnings("FieldMayBeFinal")
    @Child
    private RelationalNode relationalNode;

    protected BaseRelationalNode(KSharpNode left, KSharpNode right) {
        this.relationalNode = RelationalNodeGen.create(left, right, this);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return relationalNode.execute(frame);
    }

}
