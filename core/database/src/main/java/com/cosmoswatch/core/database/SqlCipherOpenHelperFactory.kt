package com.cosmoswatch.core.database

import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqlCipherOpenHelperFactory @Inject constructor(
    private val passphraseProvider: SqlCipherPassphraseProvider,
) {

    fun create(): SupportSQLiteOpenHelper.Factory {
        System.loadLibrary("sqlcipher")
        return SupportOpenHelperFactory(passphraseProvider.getPassphrase())
    }
}
