package com.cosmoswatch.feature.neows.data.remote

class FakeNeoWsApi(
    private val rangeResponse: (startDate: String, endDate: String) -> NeoWsFeedResponse,
) : NeoWsApi {

    var rangeError: Throwable? = null

    override suspend fun getFeed(startDate: String, endDate: String): NeoWsFeedResponse {
        rangeError?.let { throw it }
        return rangeResponse(startDate, endDate)
    }
}
