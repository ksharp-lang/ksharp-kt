package org.ksharp.doc

data class DocAbstraction(
    val name: String,
    val representation: String,
    val documentation: String = ""
)

data class Trait(
    val name: String,
    val documentation: String = "",
    val abstractions: List<DocAbstraction>,
    val impls: List<String>
)

data class Type(
    val name: String,
    val representation: String,
    val documentation: String = "",
)

interface DocModule {
    fun representation(name: String, container: String = ""): String?

    fun documentation(name: String, container: String = ""): String?

    val traits: List<Trait>
    val abstractions: List<DocAbstraction>
    val types: List<Type>
}

internal data class MemoryDocModule internal constructor(
    override val types: List<Type>,
    override val traits: List<Trait>,
    override val abstractions: List<DocAbstraction>
) : DocModule {

    private fun List<DocAbstraction>.abstraction(name: String): DocAbstraction? =
        binarySearch {
            it.name.compareTo(name)
        }.let {
            if (it < 0) return null
            else this[it]
        }

    private fun List<Trait>.trait(name: String): Trait? =
        binarySearch {
            it.name.compareTo(name)
        }.let {
            if (it < 0) return null
            else this[it]
        }

    private fun List<Type>.type(name: String): Type? =
        binarySearch {
            it.name.compareTo(name)
        }.let {
            if (it < 0) return null
            else this[it]
        }

    private fun abstractions(container: String): List<DocAbstraction> =
        if (container.isEmpty()) abstractions
        else traits.trait(container)?.abstractions ?: emptyList()

    override fun representation(name: String, container: String): String? =
        if (name.first().isUpperCase()) {
            types.type(name)?.representation
        } else abstractions(container).abstraction(name)?.representation

    override fun documentation(name: String, container: String): String? =
        if (name.first().isUpperCase()) {
            types.type(name)?.documentation
        } else abstractions(container).abstraction(name)?.documentation
}

fun docModule(
    types: List<Type>,
    traits: List<Trait>,
    abstractions: List<DocAbstraction>
): DocModule =
    MemoryDocModule(
        types.sortedBy {
            it.name
        },
        traits.sortedBy {
            it.name
        },
        abstractions.sortedBy {
            it.name
        })
