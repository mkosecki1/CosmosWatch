package com.cosmoswatch.feature.marsphotos.domain

import java.time.LocalDate

enum class MarsRover(val apiName: String, val landingDate: LocalDate) {
    CURIOSITY("curiosity", LocalDate.of(2012, 8, 6)),
    PERSEVERANCE("perseverance", LocalDate.of(2021, 2, 18)),
}
