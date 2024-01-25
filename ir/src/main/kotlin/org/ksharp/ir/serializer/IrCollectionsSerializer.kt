package org.ksharp.ir.serializer

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrCollections
import org.ksharp.ir.IrExpression
import org.ksharp.ir.IrMap
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

typealias IrCollectionFactory = (
    attribute: Set<Attribute>,
    items: List<IrExpression>,
    location: Location
) -> IrCollections

class IrCollectionsSerializer(private val factory: IrCollectionFactory) : IrNodeSerializer<IrCollections> {
    override fun write(input: IrCollections, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.location.writeTo(buffer)
        input.items.writeTo(buffer, table)
    }

    override fun read(lookup: FunctionLookup, buffer: BufferView, table: BinaryTableView): IrCollections {
        var offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)

        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16

        return factory(attributes, buffer.bufferFrom(offset).readListOfNodes(lookup, table).second.cast(), location)
    }

}


class IrMapSerializer : IrNodeSerializer<IrMap> {
    override fun write(input: IrMap, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.location.writeTo(buffer)
        input.entries.writeTo(buffer, table)
    }

    override fun read(lookup: FunctionLookup, buffer: BufferView, table: BinaryTableView): IrMap {
        var offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)

        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16

        return IrMap(attributes, buffer.bufferFrom(offset).readListOfNodes(lookup, table).second.cast(), location)
    }

}
