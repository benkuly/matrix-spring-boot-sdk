package net.folivo.matrix.core.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.folivo.matrix.core.model.MatrixId

class MatrixIdSerializer : StdSerializer<MatrixId>(MatrixId::class.java) {

    override fun serialize(value: MatrixId, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.full)
    }
}