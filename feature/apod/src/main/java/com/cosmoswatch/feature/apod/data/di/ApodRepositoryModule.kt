package com.cosmoswatch.feature.apod.data.di

import com.cosmoswatch.feature.apod.data.repository.ApodRepositoryImpl
import com.cosmoswatch.feature.apod.domain.ApodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ApodRepositoryModule {

    @Binds
    fun bindsApodRepository(impl: ApodRepositoryImpl): ApodRepository
}
