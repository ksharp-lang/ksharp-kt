package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import org.ksharp.ir.truffle.KSharpNode;

public class VarAccessNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Node.Child
    private BaseVarAccessNode node;


    public VarAccessNode(int index) {
        this.node = BaseVarAccessNodeGen.create(index);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return node.execute(frame);
    }
}
