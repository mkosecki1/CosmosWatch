package com.cosmoswatch.feature.apod.data.remote

class FakeApodApi(
    private val rangeResponse: (startDate: String, endDate: String) -> List<ApodDto>,
) : ApodApi {

    var rangeError: Throwable? = null

    override suspend fun getApod(thumbs: Boolean): ApodDto = error("not used by ApodArchiveRemoteMediatorTest")

    override suspend fun getApodRange(startDate: String, endDate: String, thumbs: Boolean): List<ApodDto> {
        rangeError?.let { throw it }
        return rangeResponse(startDate, endDate)
    }
}
