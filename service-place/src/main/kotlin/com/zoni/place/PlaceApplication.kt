package com.zoni.place

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlaceApplication

fun main(args: Array<String>) {
    runApplication<PlaceApplication>(*args)
}