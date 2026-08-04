package com.trozovka.pocketvdr.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlagEventDao {
    @Insert
    suspend fun insert(flagEvent: FlagEventEntity): Long

    @Update
    suspend fun update(flagEvent: FlagEventEntity)

    @Query("UPDATE flag_events SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String?)

    @Query("SELECT * FROM flag_events WHERE voyageId = :voyageId ORDER BY timestampMillis ASC")
    fun observeForVoyage(voyageId: Long): Flow<List<FlagEventEntity>>

    @Query("SELECT COUNT(*) FROM flag_events WHERE voyageId = :voyageId")
    suspend fun countForVoyage(voyageId: Long): Int
}
