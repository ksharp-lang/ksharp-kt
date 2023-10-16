package org.ksharp.ir.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;
import org.ksharp.common.annotation.KoverIgnore;

import java.math.BigDecimal;
import java.math.BigInteger;

@KoverIgnore(reason = "Abstract class used by truffle framework")
@TypeSystem({byte.class, short.class, int.class, long.class, float.class, double.class, BigInteger.class, BigDecimal.class})
public abstract class PreludeTypeSystem {

}
