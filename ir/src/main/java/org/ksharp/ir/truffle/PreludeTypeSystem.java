package org.ksharp.ir.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;

import java.math.BigDecimal;
import java.math.BigInteger;

@TypeSystem({byte.class, short.class, int.class, long.class, float.class, double.class, BigInteger.class, BigDecimal.class})
public abstract class PreludeTypeSystem {

}
