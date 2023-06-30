package org.ksharp.ir.truffle

import com.oracle.truffle.api.frame.VirtualFrame
import java.math.BigInteger

abstract class IntegerNode : KSharpNode() {

    abstract fun executeValue(frame: VirtualFrame): Long

    abstract fun executeBigIntValue(frame: VirtualFrame): BigInteger
    
}
