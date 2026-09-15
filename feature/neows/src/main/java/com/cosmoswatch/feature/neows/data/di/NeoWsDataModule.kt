package com.cosmoswatch.feature.neows.data.di

import android.content.Context
import androidx.room.Room
import com.cosmoswatch.core.database.SqlCipherOpenHelperFactory
import com.cosmoswatch.feature.neows.data.local.NeoArchiveDao
import com.cosmoswatch.feature.neows.data.local.NeoArchiveRemoteKeyDao
import com.cosmoswatch.feature.neows.data.local.NeoUpcomingDao
import com.cosmoswatch.feature.neows.data.local.NeoWsDatabase
import com.cosmoswatch.feature.neows.data.remote.NeoWsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NeoWsDataModule {

    @Provides
    @Singleton
    fun providesNeoWsApi(retrofit: Retrofit): NeoWsApi = retrofit.create(NeoWsApi::class.java)

    @Provides
    @Singleton
    fun providesNeoWsDatabase(
        @ApplicationContext context: Context,
        openHelperFactory: SqlCipherOpenHelperFactory,
    ): NeoWsDatabase = Room.databaseBuilder(context, NeoWsDatabase::class.java, "neows.db")
        .openHelperFactory(openHelperFactory.create())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    @Singleton
    fun providesNeoUpcomingDao(database: NeoWsDatabase): NeoUpcomingDao = database.neoUpcomingDao()

    @Provides
    @Singleton
    fun providesNeoArchiveDao(database: NeoWsDatabase): NeoArchiveDao = database.neoArchiveDao()

    @Provides
    @Singleton
    fun providesNeoArchiveRemoteKeyDao(database: NeoWsDatabase): NeoArchiveRemoteKeyDao =
        database.neoArchiveRemoteKeyDao()
}
