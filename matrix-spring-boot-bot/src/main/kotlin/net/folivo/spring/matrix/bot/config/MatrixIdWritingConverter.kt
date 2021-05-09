package net.folivo.spring.matrix.bot.config

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class MatrixIdWritingConverter : Converter<MatrixId, String> {
    override fun convert(source: MatrixId): String {
        return source.full
    }
}