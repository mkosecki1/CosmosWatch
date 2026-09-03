package com.cosmoswatch.core.database.di

import com.cosmoswatch.core.database.KeystoreSqlCipherPassphraseProvider
import com.cosmoswatch.core.database.SqlCipherPassphraseProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DatabaseModule {

    @Binds
    fun bindsSqlCipherPassphraseProvider(impl: KeystoreSqlCipherPassphraseProvider): SqlCipherPassphraseProvider
}
