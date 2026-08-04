package com.trozovka.pocketvdr.core.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One logged position sample, always UTC epoch millis for [timestampMillis]. */
@Entity(
    tableName = "fixes",
    foreignKeys = [
        ForeignKey(
            entity = VoyageEntity::class,
            parentColumns = ["id"],
            childColumns = ["voyageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("voyageId"), Index("timestampMillis")],
)
data class FixEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voyageId: Long,
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val speedMetersPerSecond: Float? = null,
    val headingDegrees: Float? = null,
    val altitudeMeters: Double? = null,
)
