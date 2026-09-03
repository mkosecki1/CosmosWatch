package com.cosmoswatch.feature.apod.data.di

import android.content.Context
import androidx.room.Room
import com.cosmoswatch.core.database.SqlCipherOpenHelperFactory
import com.cosmoswatch.feature.apod.data.local.ApodDao
import com.cosmoswatch.feature.apod.data.local.ApodDatabase
import com.cosmoswatch.feature.apod.data.remote.ApodApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApodDataModule {

    @Provides
    @Singleton
    fun providesApodApi(retrofit: Retrofit): ApodApi = retrofit.create(ApodApi::class.java)

    @Provides
    @Singleton
    fun providesApodDatabase(
        @ApplicationContext context: Context,
        openHelperFactory: SqlCipherOpenHelperFactory,
    ): ApodDatabase = Room.databaseBuilder(context, ApodDatabase::class.java, "apod.db")
        .openHelperFactory(openHelperFactory.create())
        .build()

    @Provides
    @Singleton
    fun providesApodDao(database: ApodDatabase): ApodDao = database.apodDao()
}
