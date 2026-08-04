package com.trozovka.pocketvdr.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FixDao {
    @Insert
    suspend fun insertAll(fixes: List<FixEntity>)

    @Query("SELECT * FROM fixes WHERE voyageId = :voyageId ORDER BY timestampMillis ASC")
    fun observeForVoyage(voyageId: Long): Flow<List<FixEntity>>

    @Query(
        "SELECT * FROM fixes WHERE voyageId = :voyageId AND timestampMillis BETWEEN :fromMillis AND :toMillis " +
            "ORDER BY timestampMillis ASC",
    )
    suspend fun getForVoyageInRange(voyageId: Long, fromMillis: Long, toMillis: Long): List<FixEntity>

    @Query("SELECT COUNT(*) FROM fixes WHERE voyageId = :voyageId")
    suspend fun countForVoyage(voyageId: Long): Int

    @Query("SELECT * FROM fixes WHERE voyageId = :voyageId ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun latestForVoyage(voyageId: Long): FixEntity?
}
