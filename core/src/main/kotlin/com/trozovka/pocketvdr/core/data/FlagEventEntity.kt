package com.trozovka.pocketvdr.core.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A manually flagged moment -- a near-miss, weather event, mechanical issue, or anything worth
 * finding again later without scrubbing through hours of track. [latitude]/[longitude] are the
 * most recent known fix at the moment the flag was tapped, captured then rather than looked up
 * later, so a flag always has a position even if the review screen's own fix lookup logic changes.
 */
@Entity(
    tableName = "flag_events",
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
data class FlagEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voyageId: Long,
    val timestampMillis: Long,
    val latitude: Double?,
    val longitude: Double?,
    val note: String? = null,
)
