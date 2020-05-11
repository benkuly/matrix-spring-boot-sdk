package net.folivo.matrix.bot.examples.ping

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PingApplication

fun main(args: Array<String>) {
    runApplication<PingApplication>(*args)
}