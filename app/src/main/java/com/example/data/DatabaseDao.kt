package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    @Query("SELECT * FROM pet_slots ORDER BY id ASC")
    fun getPetSlotsFlow(): Flow<List<PetSlotEntity>>

    @Query("SELECT * FROM pet_slots ORDER BY id ASC")
    suspend fun getPetSlotsList(): List<PetSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetSlots(slots: List<PetSlotEntity>)

    @Update
    suspend fun updatePetSlot(slot: PetSlotEntity)

    @Query("SELECT * FROM pet_dex ORDER BY petTypeId ASC")
    fun getPetDexFlow(): Flow<List<PetDexEntity>>

    @Query("SELECT * FROM pet_dex ORDER BY petTypeId ASC")
    suspend fun getPetDexList(): List<PetDexEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetDex(items: List<PetDexEntity>)

    @Update
    suspend fun updatePetDex(item: PetDexEntity)

    @Query("SELECT * FROM pet_collections ORDER BY itemId ASC")
    fun getPetCollectionsFlow(): Flow<List<PetCollectionEntity>>

    @Query("SELECT * FROM pet_collections ORDER BY itemId ASC")
    suspend fun getPetCollectionsList(): List<PetCollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetCollections(items: List<PetCollectionEntity>)

    @Query("SELECT * FROM garden_stats")
    fun getGardenStatsFlow(): Flow<List<GardenStatEntity>>

    @Query("SELECT * FROM garden_stats")
    suspend fun getGardenStatsList(): List<GardenStatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGardenStats(stats: List<GardenStatEntity>)

    @Query("SELECT value FROM garden_stats WHERE `key` = :key")
    suspend fun getStatValue(key: String): String?
}
