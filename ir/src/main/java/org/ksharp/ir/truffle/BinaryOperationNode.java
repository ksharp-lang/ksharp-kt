package org.ksharp.ir.truffle;

import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild("left")
@NodeChild("right")
public abstract class BinaryOperationNode extends KSharpNode {

}
