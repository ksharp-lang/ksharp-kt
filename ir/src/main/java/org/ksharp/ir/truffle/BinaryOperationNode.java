package org.ksharp.ir.truffle;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.TypeSystemReference;

@TypeSystemReference(PreludeTypeSystem.class)
@NodeChild("left")
@NodeChild("right")
public abstract class BinaryOperationNode extends KSharpNode {

}
