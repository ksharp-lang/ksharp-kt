package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import org.ksharp.ir.truffle.KSharpNode;

public class SetVarNode extends KSharpNode {

    @SuppressWarnings("FieldMayBeFinal")
    @Node.Child
    private BaseSetVarNode node;

    public SetVarNode(int slot, KSharpNode value) {
        this.node = BaseSetVarNodeGen.create(value, slot);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return node.execute(frame);
    }
}
