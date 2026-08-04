package com.trozovka.pocketvdr.core.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Thin wrapper over the three DAOs -- the review/list screens' single point of DB access. */
class VoyageRepository(context: Context) {
    private val database = PocketVdrDatabase.getInstance(context)

    fun observeVoyages(): Flow<List<VoyageEntity>> = database.voyageDao().observeAll()

    suspend fun getVoyage(voyageId: Long): VoyageEntity? = database.voyageDao().getById(voyageId)

    fun observeFixesForVoyage(voyageId: Long): Flow<List<FixEntity>> =
        database.fixDao().observeForVoyage(voyageId)

    fun observeFlagsForVoyage(voyageId: Long): Flow<List<FlagEventEntity>> =
        database.flagEventDao().observeForVoyage(voyageId)

    suspend fun fixCountForVoyage(voyageId: Long): Int = database.fixDao().countForVoyage(voyageId)

    suspend fun flagCountForVoyage(voyageId: Long): Int = database.flagEventDao().countForVoyage(voyageId)

    suspend fun updateFlagNote(flagId: Long, note: String) {
        database.flagEventDao().updateNote(flagId, note.takeIf { it.isNotBlank() })
    }

    suspend fun deleteVoyage(voyage: VoyageEntity) {
        database.voyageDao().delete(voyage)
    }
}
