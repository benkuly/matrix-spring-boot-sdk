package net.folivo.spring.matrix.bot.config

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

@ReadingConverter
class MatrixIdReadingConverter : Converter<String, MatrixId> {
    override fun convert(source: String): MatrixId {
        return MatrixId.of(source)
    }
}