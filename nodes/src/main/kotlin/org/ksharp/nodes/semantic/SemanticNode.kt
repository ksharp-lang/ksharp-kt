package org.ksharp.nodes.semantic

import org.ksharp.common.*
import org.ksharp.nodes.NoLocationsDefined
import org.ksharp.nodes.NodeData
import org.ksharp.nodes.NodeLocations
import org.ksharp.typesystem.ErrorOrType

enum class SemanticInfoErrorCode(override val description: String) : ErrorCode {
    TypeNotInferred("Type not inferred")
}

sealed interface TypePromise {
    val type: ErrorOrType
}

sealed class SemanticInfo {
    private var inferredType: ErrorOrType? = null
    fun hasInferredType(): Boolean = inferredType != null

    fun setInferredType(type: ErrorOrType) {
        inferredType = type
    }

    fun getInferredType(location: Location): ErrorOrType =
        inferredType ?: Either.Left(SemanticInfoErrorCode.TypeNotInferred.new(location))
}

data class TypeSemanticInfo(
    override val type: ErrorOrType
) : SemanticInfo(), TypePromise

data class EmptySemanticInfo(private val nothing: Unit = Unit) : SemanticInfo()

sealed class SemanticNode<SemanticInfo> : NodeData() {
    abstract val info: SemanticInfo
    override val locations: NodeLocations
        get() = NoLocationsDefined
}

fun SemanticInfo.getType(location: Location): ErrorOrType =
    when (this) {
        is TypeSemanticInfo -> if (hasInferredType()) getInferredType(location) else type
        is Symbol -> if (hasInferredType()) getInferredType(location) else type.getType(location)
        else -> getInferredType(location)
    }

fun TypePromise.getType(location: Location): ErrorOrType =
    this.cast<SemanticInfo>().getType(location)
