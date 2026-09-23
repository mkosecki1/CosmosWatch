package com.cosmoswatch.feature.donki.data.di

import android.content.Context
import androidx.room.Room
import com.cosmoswatch.core.database.SqlCipherOpenHelperFactory
import com.cosmoswatch.feature.donki.data.local.DonkiDatabase
import com.cosmoswatch.feature.donki.data.local.DonkiRemoteKeyDao
import com.cosmoswatch.feature.donki.data.local.DonkiTimelineDao
import com.cosmoswatch.feature.donki.data.remote.DonkiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DonkiDataModule {

    @Provides
    @Singleton
    fun providesDonkiApi(retrofit: Retrofit): DonkiApi = retrofit.create(DonkiApi::class.java)

    @Provides
    @Singleton
    fun providesDonkiDatabase(
        @ApplicationContext context: Context,
        openHelperFactory: SqlCipherOpenHelperFactory,
    ): DonkiDatabase = Room.databaseBuilder(context, DonkiDatabase::class.java, "donki.db")
        .openHelperFactory(openHelperFactory.create())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    @Singleton
    fun providesDonkiTimelineDao(database: DonkiDatabase): DonkiTimelineDao = database.donkiTimelineDao()

    @Provides
    @Singleton
    fun providesDonkiRemoteKeyDao(database: DonkiDatabase): DonkiRemoteKeyDao = database.donkiRemoteKeyDao()
}
