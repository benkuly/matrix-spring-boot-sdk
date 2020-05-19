package net.folivo.matrix.bot.config

class MissingRequiredPropertyException(private val property: String) : IllegalStateException() {
    override val message: String?
        get() = "The following property was declared as required but could not be resolved: $property"
}