package org.ksharp.ir.truffle.variable;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import org.ksharp.ir.KValue;
import org.ksharp.ir.truffle.KSharpNode;

import java.util.Map;

public class VarAccessNode extends KSharpNode {

    private final String captureName;
    @SuppressWarnings("FieldMayBeFinal")
    @Node.Child
    private BaseVarAccessNode node;


    public VarAccessNode(int index, String captureName) {
        this.captureName = captureName;
        this.node = BaseVarAccessNodeGen.create(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(VirtualFrame frame) {
        if (captureName != null) {
            var context = (Map<String, Object>) KValue.value(frame.getObject(0));
            return context.get(captureName);
        }
        return node.execute(frame);
    }
}
