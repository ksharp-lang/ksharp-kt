package org.ksharp.ir.truffle;


import com.oracle.truffle.api.dsl.NodeChild;
import org.ksharp.common.annotation.KoverIgnore;

@KoverIgnore(reason = "Abstract class used by truffle framework")
@NodeChild("left")
@NodeChild("right")
public abstract class KSharpBinaryNode {
}
