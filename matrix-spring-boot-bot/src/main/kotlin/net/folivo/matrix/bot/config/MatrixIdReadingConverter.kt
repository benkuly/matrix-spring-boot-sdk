package net.folivo.matrix.bot.config

import net.folivo.matrix.core.model.MatrixId
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

@ReadingConverter
class MatrixIdReadingConverter : Converter<String, MatrixId> {
    override fun convert(source: String): MatrixId {
        return MatrixId.of(source)
    }
}