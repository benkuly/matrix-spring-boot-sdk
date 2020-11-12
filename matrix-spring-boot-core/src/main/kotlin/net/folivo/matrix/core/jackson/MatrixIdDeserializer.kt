package net.folivo.matrix.core.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.folivo.matrix.core.model.MatrixId

class MatrixIdDeserializer : StdDeserializer<MatrixId>(MatrixId::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext?): MatrixId {
        return MatrixId.of(parser.text)
    }
}