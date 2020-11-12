package net.folivo.matrix.core.config

import net.folivo.matrix.core.model.MatrixId
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter

@ConfigurationPropertiesBinding
class MatrixIdConfigurationPropertiesConverter : Converter<String, MatrixId> {
    override fun convert(source: String): MatrixId {
        return MatrixId.of(source)
    }
}