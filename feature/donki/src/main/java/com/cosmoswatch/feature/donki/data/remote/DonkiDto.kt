package com.cosmoswatch.feature.donki.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkedEventDto(
    val activityID: String,
)

@Serializable
data class FlrDto(
    val flrID: String,
    val peakTime: String,
    val classType: String,
    val sourceLocation: String? = null,
    val activeRegionNum: Int? = null,
    val link: String,
    val linkedEvents: List<LinkedEventDto>? = null,
)

@Serializable
data class CmeDto(
    val activityID: String,
    val startTime: String,
    val note: String,
    val link: String,
    val cmeAnalyses: List<CmeAnalysisDto>? = null,
    val linkedEvents: List<LinkedEventDto>? = null,
)

@Serializable
data class CmeAnalysisDto(
    val isMostAccurate: Boolean,
    val speed: Double? = null,
    val enlilList: List<EnlilDto>? = null,
)

@Serializable
data class EnlilDto(
    val modelCompletionTime: String,
    val estimatedShockArrivalTime: String? = null,
    @SerialName("kp_18") val kp18: Double? = null,
    @SerialName("kp_90") val kp90: Double? = null,
    @SerialName("kp_135") val kp135: Double? = null,
    @SerialName("kp_180") val kp180: Double? = null,
)

@Serializable
data class GstDto(
    val gstID: String,
    val startTime: String,
    val allKpIndex: List<KpIndexDto>,
    val link: String,
    val linkedEvents: List<LinkedEventDto>? = null,
)

@Serializable
data class KpIndexDto(
    val observedTime: String,
    val kpIndex: Double,
)
