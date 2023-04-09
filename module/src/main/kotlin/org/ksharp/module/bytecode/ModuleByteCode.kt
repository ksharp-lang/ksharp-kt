package org.ksharp.module.bytecode

import org.ksharp.common.io.BufferWriter
import org.ksharp.typesystem.TypeSystem
import java.io.OutputStream

class ModuleByteCode(
    private val typeSystem: TypeSystem
) {
    private val stringPool = StringPoolBuilder()
    fun writeTo(output: OutputStream) {
        val stringPoolSize = stringPool.size
        val typeSize = 0
        val abstractionsSize = 0
        BufferWriterImpl().apply {
            add(stringPoolSize)
            add(typeSize)
            add(abstractionsSize)
            writeTo(output)
        }
        stringPool.writeTo(output)

        BufferWriterImpl().apply {
            writeTo(output)
        }
        BufferWriterImpl().apply {
            writeTo(output)
        }
    }
}