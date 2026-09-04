package com.cosmoswatch.feature.marsphotos.domain

import java.time.LocalDate

enum class MarsRover(val apiName: String, val landingDate: LocalDate, val cameras: List<String>) {
    CURIOSITY(
        apiName = "curiosity",
        landingDate = LocalDate.of(2012, 8, 6),
        cameras = listOf("FHAZ", "RHAZ", "MAST", "CHEMCAM", "MAHLI", "MARDI", "NAVCAM"),
    ),
    PERSEVERANCE(
        apiName = "perseverance",
        landingDate = LocalDate.of(2021, 2, 18),
        cameras = listOf(
            "EDL_RUCAM",
            "EDL_RDCAM",
            "EDL_DDCAM",
            "EDL_PUCAM1",
            "EDL_PUCAM2",
            "NAVCAM_LEFT",
            "NAVCAM_RIGHT",
            "MCZ_RIGHT",
            "MCZ_LEFT",
            "FRONT_HAZCAM_LEFT_A",
            "FRONT_HAZCAM_RIGHT_A",
            "REAR_HAZCAM_LEFT",
            "REAR_HAZCAM_RIGHT",
            "SKYCAM",
            "SHERLOC_WATSON",
        ),
    ),
}
