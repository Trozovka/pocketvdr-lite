package com.trozovka.pocketvdr.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One "start voyage" / "stop voyage" session. [endTimeMillis] is null while a voyage is active. */
@Entity(tableName = "voyages")
data class VoyageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val name: String? = null,
)
