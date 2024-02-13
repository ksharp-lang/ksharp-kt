package org.ksharp.ir.truffle.relational;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import org.ksharp.ir.KValue;
import org.ksharp.ir.truffle.KSharpNode;
import org.ksharp.ir.truffle.call.ModuleCallNode;
import org.ksharp.ir.types.Symbol;

import java.util.List;

public class ComparableNode extends KSharpNode {
    private final List<Symbol> expected;

    @SuppressWarnings("FieldMayBeFinal")
    @Node.Child
    private ModuleCallNode node;

    public ComparableNode(ModuleCallNode node, List<Symbol> expected) {
        this.node = node;
        this.expected = expected;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return expected.contains((Symbol) KValue.value(node.execute(frame)));
    }
}
