package com.trozovka.pocketvdr.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoyageDao {
    @Insert
    suspend fun insert(voyage: VoyageEntity): Long

    @Update
    suspend fun update(voyage: VoyageEntity)

    /** Cascades to that voyage's fixes and marked events via ForeignKey.CASCADE. */
    @Delete
    suspend fun delete(voyage: VoyageEntity)

    @Query("SELECT * FROM voyages ORDER BY startTimeMillis DESC")
    fun observeAll(): Flow<List<VoyageEntity>>

    @Query("SELECT * FROM voyages WHERE id = :id")
    suspend fun getById(id: Long): VoyageEntity?

    @Query("SELECT * FROM voyages WHERE endTimeMillis IS NULL LIMIT 1")
    suspend fun getActiveVoyage(): VoyageEntity?
}
