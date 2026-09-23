package com.cosmoswatch.feature.donki.data.mapper

import com.cosmoswatch.feature.donki.data.local.DonkiTimelineEntity
import com.cosmoswatch.feature.donki.data.remote.CmeAnalysisDto
import com.cosmoswatch.feature.donki.data.remote.CmeDto
import com.cosmoswatch.feature.donki.data.remote.EnlilDto
import com.cosmoswatch.feature.donki.data.remote.FlrDto
import com.cosmoswatch.feature.donki.data.remote.GstDto
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiSeverity
import com.cosmoswatch.feature.donki.domain.KpObservationDomain
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.OffsetDateTime

private const val SEVERE_KP_THRESHOLD = 8.0
private const val LINKED_EVENT_SEPARATOR = ","
private const val TYPE_FLARE = "FLARE"
private const val TYPE_CME = "CME"
private const val TYPE_STORM = "STORM"

private fun String.toInstant(): Instant = OffsetDateTime.parse(this).toInstant()

private fun flareSeverity(classType: String): DonkiSeverity = when {
    classType.startsWith("X") -> DonkiSeverity.SEVERE
    classType.startsWith("M") -> DonkiSeverity.MODERATE
    else -> DonkiSeverity.NONE
}

private fun stormSeverity(peakKp: Double): DonkiSeverity =
    if (peakKp >= SEVERE_KP_THRESHOLD) DonkiSeverity.SEVERE else DonkiSeverity.MODERATE

fun FlrDto.toDomain(): DonkiEventDomain.Flare = DonkiEventDomain.Flare(
    id = flrID,
    eventTime = peakTime.toInstant(),
    severity = flareSeverity(classType),
    linkUrl = link,
    linkedEventIds = linkedEvents.orEmpty().map { it.activityID },
    classType = classType,
    sourceLocation = sourceLocation,
    activeRegionNum = activeRegionNum,
)

private fun bestAnalysis(analyses: List<CmeAnalysisDto>?): CmeAnalysisDto? =
    analyses?.firstOrNull { it.isMostAccurate } ?: analyses?.firstOrNull()

private fun latestEnlilRun(analysis: CmeAnalysisDto?): EnlilDto? =
    analysis?.enlilList?.maxByOrNull { it.modelCompletionTime }

private fun forecastKp(enlil: EnlilDto): Double? =
    listOfNotNull(enlil.kp18, enlil.kp90, enlil.kp135, enlil.kp180).maxOrNull()

fun CmeDto.toDomain(): DonkiEventDomain.CmeEjection {
    val analysis = bestAnalysis(cmeAnalyses)
    val enlil = latestEnlilRun(analysis)
    val forecastKp = enlil?.let(::forecastKp)
    val severity = when {
        enlil == null -> DonkiSeverity.NONE
        (forecastKp ?: 0.0) >= SEVERE_KP_THRESHOLD -> DonkiSeverity.SEVERE
        else -> DonkiSeverity.MODERATE
    }
    return DonkiEventDomain.CmeEjection(
        id = activityID,
        eventTime = startTime.toInstant(),
        severity = severity,
        linkUrl = link,
        linkedEventIds = linkedEvents.orEmpty().map { it.activityID },
        note = note,
        speedKmS = analysis?.speed,
        isEarthDirected = enlil != null,
        estimatedArrivalTime = enlil?.estimatedShockArrivalTime?.toInstant(),
        forecastKp = forecastKp,
    )
}

fun GstDto.toDomain(): DonkiEventDomain.GeomagneticStorm {
    val series = allKpIndex.map { KpObservationDomain(time = it.observedTime.toInstant(), kp = it.kpIndex) }
    val peakKp = series.maxOf { it.kp }
    return DonkiEventDomain.GeomagneticStorm(
        id = gstID,
        eventTime = startTime.toInstant(),
        severity = stormSeverity(peakKp),
        linkUrl = link,
        linkedEventIds = linkedEvents.orEmpty().map { it.activityID },
        kpIndexSeries = series,
        peakKp = peakKp,
    )
}

@Serializable
private data class KpPointJson(val epochMillis: Long, val kp: Double)

private fun List<KpObservationDomain>.toJson(): String =
    Json.encodeToString(map { KpPointJson(it.time.toEpochMilli(), it.kp) })

private fun String.toKpSeries(): List<KpObservationDomain> =
    Json.decodeFromString<List<KpPointJson>>(this).map { KpObservationDomain(Instant.ofEpochMilli(it.epochMillis), it.kp) }

private fun List<String>.toJoinedString(): String = joinToString(LINKED_EVENT_SEPARATOR)

private fun String.toLinkedEventIds(): List<String> = if (isEmpty()) emptyList() else split(LINKED_EVENT_SEPARATOR)

fun DonkiEventDomain.toEntity(page: Int): DonkiTimelineEntity = when (this) {
    is DonkiEventDomain.Flare -> DonkiTimelineEntity(
        id = id,
        type = TYPE_FLARE,
        eventTimeEpochMillis = eventTime.toEpochMilli(),
        severity = severity.name,
        linkUrl = linkUrl,
        linkedEventIds = linkedEventIds.toJoinedString(),
        page = page,
        flareClassType = classType,
        flareSourceLocation = sourceLocation,
        flareActiveRegionNum = activeRegionNum,
    )
    is DonkiEventDomain.CmeEjection -> DonkiTimelineEntity(
        id = id,
        type = TYPE_CME,
        eventTimeEpochMillis = eventTime.toEpochMilli(),
        severity = severity.name,
        linkUrl = linkUrl,
        linkedEventIds = linkedEventIds.toJoinedString(),
        page = page,
        cmeNote = note,
        cmeSpeedKmS = speedKmS,
        cmeIsEarthDirected = isEarthDirected,
        cmeEstimatedArrivalEpochMillis = estimatedArrivalTime?.toEpochMilli(),
        cmeForecastKp = forecastKp,
    )
    is DonkiEventDomain.GeomagneticStorm -> DonkiTimelineEntity(
        id = id,
        type = TYPE_STORM,
        eventTimeEpochMillis = eventTime.toEpochMilli(),
        severity = severity.name,
        linkUrl = linkUrl,
        linkedEventIds = linkedEventIds.toJoinedString(),
        page = page,
        stormPeakKp = peakKp,
        stormKpSeriesJson = kpIndexSeries.toJson(),
    )
}

fun DonkiTimelineEntity.toDomain(): DonkiEventDomain {
    val eventTimeValue = Instant.ofEpochMilli(eventTimeEpochMillis)
    val severityValue = DonkiSeverity.valueOf(severity)
    val linkedIds = linkedEventIds.toLinkedEventIds()
    return when (type) {
        TYPE_FLARE -> DonkiEventDomain.Flare(
            id = id,
            eventTime = eventTimeValue,
            severity = severityValue,
            linkUrl = linkUrl,
            linkedEventIds = linkedIds,
            classType = requireNotNull(flareClassType),
            sourceLocation = flareSourceLocation,
            activeRegionNum = flareActiveRegionNum,
        )
        TYPE_CME -> DonkiEventDomain.CmeEjection(
            id = id,
            eventTime = eventTimeValue,
            severity = severityValue,
            linkUrl = linkUrl,
            linkedEventIds = linkedIds,
            note = requireNotNull(cmeNote),
            speedKmS = cmeSpeedKmS,
            isEarthDirected = requireNotNull(cmeIsEarthDirected),
            estimatedArrivalTime = cmeEstimatedArrivalEpochMillis?.let(Instant::ofEpochMilli),
            forecastKp = cmeForecastKp,
        )
        TYPE_STORM -> DonkiEventDomain.GeomagneticStorm(
            id = id,
            eventTime = eventTimeValue,
            severity = severityValue,
            linkUrl = linkUrl,
            linkedEventIds = linkedIds,
            kpIndexSeries = requireNotNull(stormKpSeriesJson).toKpSeries(),
            peakKp = requireNotNull(stormPeakKp),
        )
        else -> error("Unknown DONKI event type: $type")
    }
}
