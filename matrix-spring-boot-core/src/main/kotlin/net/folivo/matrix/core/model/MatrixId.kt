package net.folivo.matrix.core.model

//TODO do we need validation?
sealed class MatrixId {

    val full: String
    val sigilCharacter: Char
    val localpart: String
    val domain: String

    constructor(full: String, sigilCharacter: Char) {
        this.full = full
        this.sigilCharacter = sigilCharacter
        val s = full.trimStart(sigilCharacter)
        localpart = s.substringBefore(':')
        domain = s.substringAfter(':')

        if (full.isEmpty()) throw IllegalArgumentException("matrix identifier must not be empty")
        if (sigilCharacter != full.first()) throw IllegalArgumentException("given sigil character $sigilCharacter does not match with full string $full")
        if (localpart.isEmpty()) throw IllegalArgumentException("localpart must not be empty")
        if (domain.isEmpty()) throw IllegalArgumentException("domain must not be empty")
    }

    constructor(localpart: String, domain: String, sigilCharacter: Char) {
        this.full = "$sigilCharacter$localpart:$domain"
        this.sigilCharacter = sigilCharacter
        this.localpart = localpart
        this.domain = domain
    }

    companion object {
        @JvmStatic
        fun of(full: String): MatrixId {
            if (full.isEmpty()) throw IllegalArgumentException("matrix identifier must not be empty")
            return when (full.first()) {
                '@' -> UserId(full)
                '!' -> RoomId(full)
                '#' -> RoomAliasId(full)
                '$' -> EventId(full)
                else -> throw IllegalArgumentException("not a valid matrix identifier")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        // self check
        if (this === other) return true
        // null check
        if (other == null) return false

        return if (other is MatrixId) {
            other.full == this.full
        } else return false
    }

    override fun hashCode(): Int {
        return full.hashCode()
    }

    override fun toString(): String {
        return full
    }

    class UserId : MatrixId {
        constructor(full: String) : super(full, '@')
        constructor(localpart: String, domain: String) : super(localpart, domain, '@')
    }

    class RoomId : MatrixId {
        constructor(full: String) : super(full, '!')
        constructor(localpart: String, domain: String) : super(localpart, domain, '!')
    }

    class RoomAliasId : MatrixId {
        constructor(full: String) : super(full, '#')
        constructor(localpart: String, domain: String) : super(localpart, domain, '#')
    }

    class EventId : MatrixId {
        constructor(full: String) : super(full, '$')
        constructor(localpart: String, domain: String) : super(localpart, domain, '$')
    }
}