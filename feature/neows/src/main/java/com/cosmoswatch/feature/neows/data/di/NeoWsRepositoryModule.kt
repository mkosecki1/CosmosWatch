package com.cosmoswatch.feature.neows.data.di

import com.cosmoswatch.feature.neows.data.repository.NeoWsRepositoryImpl
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NeoWsRepositoryModule {

    @Binds
    fun bindsNeoWsRepository(impl: NeoWsRepositoryImpl): NeoWsRepository
}
