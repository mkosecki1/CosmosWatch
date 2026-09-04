package com.cosmoswatch.feature.marsphotos.data.di

import com.cosmoswatch.feature.marsphotos.data.repository.MarsPhotosRepositoryImpl
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MarsPhotosRepositoryModule {

    @Binds
    fun bindsMarsPhotosRepository(impl: MarsPhotosRepositoryImpl): MarsPhotosRepository
}
