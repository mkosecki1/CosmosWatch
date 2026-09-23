package com.cosmoswatch.feature.donki.data.di

import com.cosmoswatch.feature.donki.data.repository.DonkiRepositoryImpl
import com.cosmoswatch.feature.donki.domain.DonkiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DonkiRepositoryModule {

    @Binds
    fun bindsDonkiRepository(impl: DonkiRepositoryImpl): DonkiRepository
}
