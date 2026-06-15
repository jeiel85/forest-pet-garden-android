package com.jeiel85.forestpetgarden.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_slots")
data class PetSlotEntity(
    @PrimaryKey val id: Int, // 0 to 5 (6 slots maximum)
    val petTypeId: Int?,     // null if empty/no pet, otherwise 1 to 8
    val petName: String,     // Custom customizable pet name
    val growthPercent: Float,// 0.0f to 1.0f. Level up when 1.0f
    val hungerPercent: Float,// 0.0f to 1.0f (1.0f is full, depletes over time)
    val happinessPercent: Float, // 0.0f to 1.0f (increases when playing)
    val level: Int,          // Pet level, starts at 1
    val activityStatus: Int, // 0 = Idle, 1 = Exploring forest, 2 = Soft sleep
    val activityEndTime: Long,// UTC timestamp when current exploration or action completes
    val activityAreaId: Int?,// The area ID they are exploring
    val lastUpdatedAt: Long  // Timestamp for off-line progress calculation
)

@Entity(tableName = "pet_dex")
data class PetDexEntity(
    @PrimaryKey val petTypeId: Int, // 1 to 8 representing the pet kinds
    val unlocked: Boolean,
    val adoptCount: Int
)

@Entity(tableName = "pet_collections")
data class PetCollectionEntity(
    @PrimaryKey val itemId: Int, // 1 to 12 representing unique items
    val foundCount: Int,
    val discoveredTime: Long
)

@Entity(tableName = "garden_stats") // Keep table name for compatibility
data class GardenStatEntity(
    @PrimaryKey val key: String,
    val value: String
)
