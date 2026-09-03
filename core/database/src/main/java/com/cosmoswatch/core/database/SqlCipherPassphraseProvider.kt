package com.cosmoswatch.core.database

interface SqlCipherPassphraseProvider {
    fun getPassphrase(): ByteArray
}
