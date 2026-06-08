package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GardenRepository(private val gardenDao: GardenDao) {

    val petSlotsFlow: Flow<List<PetSlotEntity>> = gardenDao.getPetSlotsFlow()
    val petDexFlow: Flow<List<PetDexEntity>> = gardenDao.getPetDexFlow()
    val petCollectionsFlow: Flow<List<PetCollectionEntity>> = gardenDao.getPetCollectionsFlow()
    val gardenStatsFlow: Flow<List<GardenStatEntity>> = gardenDao.getGardenStatsFlow()

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val currentSlots = gardenDao.getPetSlotsList()
        if (currentSlots.isEmpty()) {
            // Initialize 6 slots: Slot 0 has a cozy Baby Fox 'Ara' by default! Others empty baskets.
            val initialSlots = (0..5).map { index ->
                if (index == 0) {
                    PetSlotEntity(
                        id = index,
                        petTypeId = 1, // Ara
                        petName = "아라",
                        growthPercent = 0.15f,
                        hungerPercent = 0.8f,
                        happinessPercent = 0.9f,
                        level = 1,
                        activityStatus = 0, // Idle
                        activityEndTime = 0L,
                        activityAreaId = null,
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                } else {
                    PetSlotEntity(
                        id = index,
                        petTypeId = null, // Empty
                        petName = "",
                        growthPercent = 0.0f,
                        hungerPercent = 1.0f,
                        happinessPercent = 1.0f,
                        level = 1,
                        activityStatus = 0,
                        activityEndTime = 0L,
                        activityAreaId = null,
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                }
            }
            gardenDao.insertPetSlots(initialSlots)
        }

        val currentDex = gardenDao.getPetDexList()
        if (currentDex.isEmpty()) {
            // Initialize all 8 pet species
            // Baby Fox (Id 1) and Sleepy Koala (Id 2) adopted/discovered by default.
            val initialDex = (1..8).map { id ->
                PetDexEntity(
                    petTypeId = id,
                    unlocked = id <= 2,
                    adoptCount = if (id == 1) 1 else 0
                )
            }
            gardenDao.insertPetDex(initialDex)
        }

        val currentStats = gardenDao.getGardenStatsList()
        if (currentStats.isEmpty()) {
            // Initialize statistics. Dew is Stellar Sparks (Isc) used to adopt or feed.
            val initialStats = listOf(
                GardenStatEntity("dew_count", "300"), // ✨ Star Sparks currency
                GardenStatEntity("garden_xp", "0"),   // Little Forest Keeper XP
                GardenStatEntity("garden_level", "1"),
                // Tools / Toys levels
                GardenStatEntity("toy_level", "1"),   // Toy/Play level
                // Sleeping basket slots unlocked. Slots 0,1,2 unlocked. 3,4,5 need purchase
                GardenStatEntity("slot_unlocked_3", "false"),
                GardenStatEntity("slot_unlocked_4", "false"),
                GardenStatEntity("slot_unlocked_5", "false"),
                // Timestamp
                GardenStatEntity("last_visit_time", System.currentTimeMillis().toString())
            )
            gardenDao.insertGardenStats(initialStats)
        }
    }

    // --- Slot Operations ---
    suspend fun getPetSlotsList(): List<PetSlotEntity> = withContext(Dispatchers.IO) {
        gardenDao.getPetSlotsList()
    }

    suspend fun savePetSlots(slots: List<PetSlotEntity>) = withContext(Dispatchers.IO) {
        gardenDao.insertPetSlots(slots)
    }

    suspend fun updatePetSlot(slot: PetSlotEntity) = withContext(Dispatchers.IO) {
        gardenDao.updatePetSlot(slot)
    }

    // --- Pet Dex Operations ---
    suspend fun updatePetDex(petTypeId: Int, unlocked: Boolean, adoptCountIncrement: Int) = withContext(Dispatchers.IO) {
        val list = gardenDao.getPetDexList()
        val match = list.find { it.petTypeId == petTypeId }
        val updated = if (match != null) {
            PetDexEntity(
                petTypeId = petTypeId,
                unlocked = unlocked || match.unlocked,
                adoptCount = match.adoptCount + adoptCountIncrement
            )
        } else {
            PetDexEntity(petTypeId = petTypeId, unlocked = unlocked, adoptCount = adoptCountIncrement)
        }
        databaseInsertPetDexDirect(updated)
    }

    private suspend fun databaseInsertPetDexDirect(entity: PetDexEntity) {
        gardenDao.insertPetDex(listOf(entity))
    }

    // --- Collections ---
    suspend fun addCollectionItem(itemId: Int) = withContext(Dispatchers.IO) {
        val current = gardenDao.getPetCollectionsList()
        val match = current.find { it.itemId == itemId }
        val updated = if (match != null) {
            PetCollectionEntity(
                itemId = itemId,
                foundCount = match.foundCount + 1,
                discoveredTime = System.currentTimeMillis()
            )
        } else {
            PetCollectionEntity(
                itemId = itemId,
                foundCount = 1,
                discoveredTime = System.currentTimeMillis()
            )
        }
        gardenDao.insertPetCollections(listOf(updated))
    }

    // --- Stats Operations ---
    suspend fun getStatsMap(): Map<String, String> = withContext(Dispatchers.IO) {
        val list = gardenDao.getGardenStatsList()
        list.associate { it.key to it.value }
    }

    suspend fun saveStat(key: String, value: String) = withContext(Dispatchers.IO) {
        gardenDao.insertGardenStats(listOf(GardenStatEntity(key, value)))
    }

    suspend fun saveStats(stats: Map<String, String>) = withContext(Dispatchers.IO) {
        val entities = stats.map { GardenStatEntity(it.key, it.value) }
        gardenDao.insertGardenStats(entities)
    }
}
