package com.cosmoswatch.feature.marsphotos.data.di

import android.content.Context
import androidx.room.Room
import com.cosmoswatch.core.database.SqlCipherOpenHelperFactory
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotosDatabase
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotosApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarsPhotosDataModule {

    @Provides
    @Singleton
    fun providesMarsPhotosApi(retrofit: Retrofit): MarsPhotosApi = retrofit.create(MarsPhotosApi::class.java)

    @Provides
    @Singleton
    fun providesMarsPhotosDatabase(
        @ApplicationContext context: Context,
        openHelperFactory: SqlCipherOpenHelperFactory,
    ): MarsPhotosDatabase = Room.databaseBuilder(context, MarsPhotosDatabase::class.java, "mars_photos.db")
        .openHelperFactory(openHelperFactory.create())
        .build()
}
