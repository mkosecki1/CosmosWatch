package com.cosmoswatch.core.network.di

import com.cosmoswatch.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

private const val NASA_BASE_URL = "https://api.nasa.gov/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun providesApiKeyInterceptor(): Interceptor = Interceptor { chain ->
        val requestWithKey = chain.request().let { request ->
            request.newBuilder()
                .url(
                    request.url.newBuilder()
                        .addQueryParameter("api_key", BuildConfig.NASA_API_KEY)
                        .build(),
                )
                .build()
        }
        chain.proceed(requestWithKey)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(apiKeyInterceptor: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                }
            }
            .build()

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(NASA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
