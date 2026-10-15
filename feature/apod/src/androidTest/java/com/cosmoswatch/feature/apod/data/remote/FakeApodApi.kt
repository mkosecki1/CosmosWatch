package com.cosmoswatch.feature.apod.data.remote

class FakeApodApi(
    private val pageResponse: (page: Int, perPage: Int) -> List<ApodDto>,
) : ApodApi {

    var pageError: Throwable? = null

    override suspend fun getPage(page: Int, perPage: Int): List<ApodDto> {
        pageError?.let { throw it }
        return pageResponse(page, perPage)
    }
}
