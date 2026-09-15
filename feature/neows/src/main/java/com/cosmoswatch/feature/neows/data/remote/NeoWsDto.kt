package com.cosmoswatch.feature.neows.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeoWsFeedResponse(
    @SerialName("near_earth_objects") val nearEarthObjects: Map<String, List<NeoDto>>,
)

@Serializable
data class NeoDto(
    val id: String,
    val name: String,
    @SerialName("is_potentially_hazardous_asteroid") val isPotentiallyHazardousAsteroid: Boolean,
    @SerialName("is_sentry_object") val isSentryObject: Boolean,
    @SerialName("estimated_diameter") val estimatedDiameter: EstimatedDiameterDto,
    @SerialName("close_approach_data") val closeApproachData: List<CloseApproachDto>,
)

@Serializable
data class EstimatedDiameterDto(
    val meters: DiameterRangeDto,
)

@Serializable
data class DiameterRangeDto(
    @SerialName("estimated_diameter_min") val estimatedDiameterMin: Double,
    @SerialName("estimated_diameter_max") val estimatedDiameterMax: Double,
)

@Serializable
data class CloseApproachDto(
    @SerialName("close_approach_date") val closeApproachDate: String,
    @SerialName("relative_velocity") val relativeVelocity: RelativeVelocityDto,
    @SerialName("miss_distance") val missDistance: MissDistanceDto,
)

@Serializable
data class RelativeVelocityDto(
    @SerialName("kilometers_per_hour") val kilometersPerHour: String,
)

@Serializable
data class MissDistanceDto(
    val lunar: String,
)
