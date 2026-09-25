package com.cosmoswatch.feature.donki.data.mapper

import com.cosmoswatch.feature.donki.data.remote.CmeAnalysisDto
import com.cosmoswatch.feature.donki.data.remote.CmeDto
import com.cosmoswatch.feature.donki.data.remote.EnlilDto
import com.cosmoswatch.feature.donki.data.remote.FlrDto
import com.cosmoswatch.feature.donki.data.remote.GstDto
import com.cosmoswatch.feature.donki.data.remote.KpIndexDto
import com.cosmoswatch.feature.donki.data.remote.LinkedEventDto
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiSeverity
import com.cosmoswatch.feature.donki.domain.KpObservationDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class DonkiMappersTest {

    @Test
    fun `flare peak time parses despite DONKI omitting seconds`() {
        val flr = FlrDto(
            flrID = "2024-05-08T05:09:00-FLR-001",
            peakTime = "2024-05-08T05:09Z",
            classType = "X1.0",
            sourceLocation = "S22W10",
            activeRegionNum = 3664,
            link = "https://webtools.ccmc.gsfc.nasa.gov/DONKI/view/FLR/1/-1",
            linkedEvents = null,
        )

        val domain = flr.toDomain()

        assertEquals(Instant.parse("2024-05-08T05:09:00Z"), domain.eventTime)
    }

    @Test
    fun `X-class flare is severe, M-class is moderate, C-class is not significant`() {
        val base = FlrDto(
            flrID = "id",
            peakTime = "2024-05-08T05:09Z",
            classType = "X1.0",
            sourceLocation = null,
            activeRegionNum = null,
            link = "link",
        )

        assertEquals(DonkiSeverity.SEVERE, base.copy(classType = "X1.0").toDomain().severity)
        assertEquals(DonkiSeverity.MODERATE, base.copy(classType = "M3.4").toDomain().severity)
        assertEquals(DonkiSeverity.NONE, base.copy(classType = "C2.1").toDomain().severity)
    }

    @Test
    fun `flare carries its linked event ids through`() {
        val flr = FlrDto(
            flrID = "id",
            peakTime = "2024-05-08T05:09Z",
            classType = "X1.0",
            sourceLocation = null,
            activeRegionNum = null,
            link = "link",
            linkedEvents = listOf(LinkedEventDto("2024-05-08T05:36:00-CME-001")),
        )

        assertEquals(listOf("2024-05-08T05:36:00-CME-001"), flr.toDomain().linkedEventIds)
    }

    private fun enlil(modelCompletionTime: String, arrival: String?, kp90: Double?, kp135: Double?) = EnlilDto(
        modelCompletionTime = modelCompletionTime,
        estimatedShockArrivalTime = arrival,
        kp18 = null,
        kp90 = kp90,
        kp135 = kp135,
        kp180 = null,
    )

    @Test
    fun `Earth-directed CME picks the latest Enlil run and its highest forecast Kp`() {
        val cme = CmeDto(
            activityID = "2024-05-08T05:36:00-CME-001",
            startTime = "2024-05-08T05:36Z",
            note = "Earth-directed full halo CME.",
            link = "link",
            cmeAnalyses = listOf(
                CmeAnalysisDto(
                    isMostAccurate = true,
                    speed = 870.0,
                    enlilList = listOf(
                        enlil(modelCompletionTime = "2024-05-08T18:23Z", arrival = "2024-05-10T12:14Z", kp90 = 6.0, kp135 = 7.0),
                        enlil(modelCompletionTime = "2024-05-09T20:28Z", arrival = "2024-05-10T13:03Z", kp90 = 8.0, kp135 = 9.0),
                    ),
                ),
            ),
        )

        val domain = cme.toDomain()

        assertTrue(domain.isEarthDirected)
        assertEquals(870.0, domain.speedKmS)
        assertEquals(Instant.parse("2024-05-10T13:03:00Z"), domain.estimatedArrivalTime)
        assertEquals(9.0, domain.forecastKp)
        assertEquals(DonkiSeverity.SEVERE, domain.severity)
    }

    @Test
    fun `CME with no Enlil run is not Earth-directed and carries no forecast`() {
        val cme = CmeDto(
            activityID = "id",
            startTime = "2024-05-09T09:12Z",
            note = "Faint CME, not directed at Earth.",
            link = "link",
            cmeAnalyses = listOf(CmeAnalysisDto(isMostAccurate = true, speed = 400.0, enlilList = emptyList())),
        )

        val domain = cme.toDomain()

        assertFalse(domain.isEarthDirected)
        assertNull(domain.estimatedArrivalTime)
        assertNull(domain.forecastKp)
        assertEquals(DonkiSeverity.NONE, domain.severity)
    }

    @Test
    fun `CME modelled by Enlil without an Earth arrival is not Earth-directed`() {
        val cme = CmeDto(
            activityID = "id",
            startTime = "2024-05-11T16:24Z",
            note = "Enlil run shows impacts on STEREO A and Parker Solar Probe only.",
            link = "link",
            cmeAnalyses = listOf(
                CmeAnalysisDto(
                    isMostAccurate = true,
                    speed = 650.0,
                    enlilList = listOf(enlil(modelCompletionTime = "2024-05-11T20:00Z", arrival = null, kp90 = null, kp135 = null)),
                ),
            ),
        )

        val domain = cme.toDomain()

        assertFalse(domain.isEarthDirected)
        assertNull(domain.estimatedArrivalTime)
        assertNull(domain.forecastKp)
        assertEquals(DonkiSeverity.NONE, domain.severity)
    }

    @Test
    fun `CME with a moderate forecast Kp is moderate severity, not severe`() {
        val cme = CmeDto(
            activityID = "id",
            startTime = "2024-05-08T05:36Z",
            note = "note",
            link = "link",
            cmeAnalyses = listOf(
                CmeAnalysisDto(
                    isMostAccurate = true,
                    speed = 700.0,
                    enlilList = listOf(enlil(modelCompletionTime = "2024-05-08T18:23Z", arrival = "2024-05-10T18:29Z", kp90 = 5.0, kp135 = 7.0)),
                ),
            ),
        )

        assertEquals(DonkiSeverity.MODERATE, cme.toDomain().severity)
    }

    @Test
    fun `storm peak Kp and severity come from the observed Kp series`() {
        val gst = GstDto(
            gstID = "2024-05-10T15:00:00-GST-001",
            startTime = "2024-05-10T15:00Z",
            allKpIndex = listOf(
                KpIndexDto(observedTime = "2024-05-10T18:00Z", kpIndex = 7.67),
                KpIndexDto(observedTime = "2024-05-11T00:00Z", kpIndex = 9.0),
                KpIndexDto(observedTime = "2024-05-12T03:00Z", kpIndex = 6.67),
            ),
            link = "link",
            linkedEvents = listOf(
                LinkedEventDto("2024-05-08T05:36:00-CME-001"),
                LinkedEventDto("2024-05-08T12:24:00-CME-001"),
            ),
        )

        val domain = gst.toDomain()

        assertEquals(9.0, domain.peakKp)
        assertEquals(DonkiSeverity.SEVERE, domain.severity)
        assertEquals(3, domain.kpIndexSeries.size)
        assertEquals(2, domain.linkedEventIds.size)
    }

    @Test
    fun `a G1-G3 storm (peak Kp below 8) is moderate, never treated as insignificant`() {
        val gst = GstDto(
            gstID = "id",
            startTime = "2024-01-01T00:00Z",
            allKpIndex = listOf(KpIndexDto(observedTime = "2024-01-01T00:00Z", kpIndex = 6.0)),
            link = "link",
        )

        assertEquals(DonkiSeverity.MODERATE, gst.toDomain().severity)
    }

    @Test
    fun `flare round-trips through its Room entity`() {
        val flare = DonkiEventDomain.Flare(
            id = "2024-05-08T05:09:00-FLR-001",
            eventTime = Instant.parse("2024-05-08T05:09:00Z"),
            severity = DonkiSeverity.SEVERE,
            linkUrl = "link",
            linkedEventIds = listOf("2024-05-08T05:36:00-CME-001"),
            classType = "X1.0",
            sourceLocation = "S22W10",
            activeRegionNum = 3664,
        )

        assertEquals(flare, flare.toEntity(page = 2).toDomain())
    }

    @Test
    fun `Earth-directed CME round-trips through its Room entity`() {
        val cme = DonkiEventDomain.CmeEjection(
            id = "2024-05-08T05:36:00-CME-001",
            eventTime = Instant.parse("2024-05-08T05:36:00Z"),
            severity = DonkiSeverity.SEVERE,
            linkUrl = "link",
            linkedEventIds = listOf("2024-05-08T05:09:00-FLR-001"),
            note = "Earth-directed full halo CME.",
            speedKmS = 870.0,
            isEarthDirected = true,
            estimatedArrivalTime = Instant.parse("2024-05-10T13:03:00Z"),
            forecastKp = 9.0,
        )

        assertEquals(cme, cme.toEntity(page = 0).toDomain())
    }

    @Test
    fun `not Earth-directed CME round-trips with a null arrival time`() {
        val cme = DonkiEventDomain.CmeEjection(
            id = "id",
            eventTime = Instant.parse("2024-05-09T09:12:00Z"),
            severity = DonkiSeverity.NONE,
            linkUrl = "link",
            linkedEventIds = emptyList(),
            note = "note",
            speedKmS = 400.0,
            isEarthDirected = false,
            estimatedArrivalTime = null,
            forecastKp = null,
        )

        assertEquals(cme, cme.toEntity(page = 0).toDomain())
    }

    @Test
    fun `storm's Kp series round-trips through its JSON column`() {
        val storm = DonkiEventDomain.GeomagneticStorm(
            id = "2024-05-10T15:00:00-GST-001",
            eventTime = Instant.parse("2024-05-10T15:00:00Z"),
            severity = DonkiSeverity.SEVERE,
            linkUrl = "link",
            linkedEventIds = listOf("2024-05-08T05:36:00-CME-001", "2024-05-08T12:24:00-CME-001"),
            kpIndexSeries = listOf(
                KpObservationDomain(Instant.parse("2024-05-10T18:00:00Z"), 7.67),
                KpObservationDomain(Instant.parse("2024-05-11T00:00:00Z"), 9.0),
            ),
            peakKp = 9.0,
        )

        assertEquals(storm, storm.toEntity(page = 0).toDomain())
    }
}
