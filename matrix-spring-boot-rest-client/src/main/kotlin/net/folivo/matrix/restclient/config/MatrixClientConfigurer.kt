package net.folivo.matrix.restclient.config

interface MatrixClientConfigurer {
    fun configure(config: MatrixClientConfiguration)
}