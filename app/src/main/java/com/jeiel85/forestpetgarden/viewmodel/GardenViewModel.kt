package com.jeiel85.forestpetgarden.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jeiel85.forestpetgarden.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

data class OfflineRewardDetails(
    val elapsedSeconds: Long,
    val hungerDecreased: Float,
    val adventuresCompleted: List<String>, // "샤샤가 반짝 별빛 조약돌을 물고 돌아왔습니다!"
    val sparksGained: Int,
    val showDialog: Boolean
)

data class AdventureCompletedReveal(
    val petName: String,
    val petEmoji: String,
    val areaName: String,
    val itemId: Int,
    val itemName: String,
    val itemEmoji: String,
    val itemRarity: Rarity,
    val itemLore: String,
    val sparksReward: Int,
    val keeperXpReward: Int
)

class GardenViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = GardenRepository(db.gardenDao)

    // Flowing States from Database
    val petSlots: Flow<List<PetSlotEntity>> = repository.petSlotsFlow
    val petDex: Flow<List<PetDexEntity>> = repository.petDexFlow
    val petCollections: Flow<List<PetCollectionEntity>> = repository.petCollectionsFlow

    // Local state representing our dynamic stats
    private val _dewCount = MutableStateFlow(300)
    val dewCount = _dewCount.asStateFlow()

    private val _gardenLevel = MutableStateFlow(1)
    val gardenLevel = _gardenLevel.asStateFlow()

    private val _gardenXp = MutableStateFlow(0)
    val gardenXp = _gardenXp.asStateFlow()

    // Upgrades
    private val _toyLevel = MutableStateFlow(1)
    val toyLevel = _toyLevel.asStateFlow()

    // Backward compatibility property for Main Screen or Layout checks
    val wateringCanLvl: StateFlow<Int> = _toyLevel.asStateFlow()
    val soilLvl = MutableStateFlow(1)
    val musicLvl = MutableStateFlow(1)

    // Slots unlocked
    private val _slotUnlocked3 = MutableStateFlow(false)
    val slotUnlocked3 = _slotUnlocked3.asStateFlow()

    private val _slotUnlocked4 = MutableStateFlow(false)
    val slotUnlocked4 = _slotUnlocked4.asStateFlow()

    private val _slotUnlocked5 = MutableStateFlow(false)
    val slotUnlocked5 = _slotUnlocked5.asStateFlow()

    // Offline dialog reward alert state
    private val _offlineReward = MutableStateFlow<OfflineRewardDetails?>(null)
    val offlineReward = _offlineReward.asStateFlow()

    // Adventure completed overlay reveal
    private val _adventureCompletedReveal = MutableStateFlow<AdventureCompletedReveal?>(null)
    val adventureCompletedReveal = _adventureCompletedReveal.asStateFlow()

    // Particles/Music floating effects for taps
    private val _tapNotes = MutableStateFlow<List<Pair<Int, String>>>(emptyList()) // Pair of Slot ID and floating emoji (e.g. ❤️, 🐾)
    val tapNotes = _tapNotes.asStateFlow()

    // Active leveling milestone celebration dialog state
    private val _levelUpCelebration = MutableStateFlow<Int?>(null) // Contains level reached if just leveled up
    val levelUpCelebration = _levelUpCelebration.asStateFlow()

    init {
        viewModelScope.launch {
            // Initialize database collections
            repository.initializeIfNeeded()

            // Fetch current stats from database
            loadStatsFromDb()

            // Calculate offline progression
            calculateOfflineGains()

            // Start standard game loop ticking every second
            startGameLoop()
        }
    }

    private suspend fun loadStatsFromDb() {
        val stats = repository.getStatsMap()
        _dewCount.value = stats["dew_count"]?.toIntOrNull() ?: 300
        _gardenLevel.value = stats["garden_level"]?.toIntOrNull() ?: 1
        _gardenXp.value = stats["garden_xp"]?.toIntOrNull() ?: 0

        _toyLevel.value = stats["toy_level"]?.toIntOrNull() ?: 1

        _slotUnlocked3.value = stats["slot_unlocked_3"]?.toBoolean() ?: false
        _slotUnlocked4.value = stats["slot_unlocked_4"]?.toBoolean() ?: false
        _slotUnlocked5.value = stats["slot_unlocked_5"]?.toBoolean() ?: false
    }

    private suspend fun saveStatsToDb() {
        val statsToWrite = mapOf(
            "dew_count" to _dewCount.value.toString(),
            "garden_level" to _gardenLevel.value.toString(),
            "garden_xp" to _gardenXp.value.toString(),
            "toy_level" to _toyLevel.value.toString(),
            "slot_unlocked_3" to _slotUnlocked3.value.toString(),
            "slot_unlocked_4" to _slotUnlocked4.value.toString(),
            "slot_unlocked_5" to _slotUnlocked5.value.toString(),
            "last_visit_time" to System.currentTimeMillis().toString()
        )
        repository.saveStats(statsToWrite)
    }

    // --- Core Game Loop ---
    private fun startGameLoop() {
        viewModelScope.launch {
            var saveCounter = 0
            while (true) {
                delay(1000L) // Wait 1 second
                tickGame(1L) // Process 1 second of pet states

                saveCounter++
                if (saveCounter >= 5) {
                    saveStatsToDb() // Auto save every 5 seconds
                    saveCounter = 0
                }
            }
        }
    }

    private suspend fun tickGame(deltaSeconds: Long) {
        val currentSlots = repository.getPetSlotsList()
        val now = System.currentTimeMillis()
        val updatedSlots = currentSlots.map { slot ->
            if (slot.petTypeId == null) {
                slot
            } else {
                // 1. Gentle hunger decay over time (takes 8 hours to completely deplete)
                val decayRate = 1.0f / (8 * 3600f)
                val nextHunger = (slot.hungerPercent - decayRate * deltaSeconds).coerceIn(0.1f, 1.0f)

                // 2. Gentle happiness decay over time (takes 12 hours to completely deplete if neglected)
                val happyDecay = 1.0f / (12 * 3600f)
                val nextHappiness = (slot.happinessPercent - happyDecay * deltaSeconds).coerceIn(0.2f, 1.0f)

                // Check if exploration completed while running
                if (slot.activityStatus == 1 && slot.activityEndTime > 0L && now >= slot.activityEndTime) {
                    // Note: We don't auto-resolve exploration so that players get the fun of clicking and opening chest!
                    // So we keep status as 1 and let player click "수확(REWARD/RETURN)" to reveal it.
                    slot.copy(
                        hungerPercent = nextHunger,
                        happinessPercent = nextHappiness,
                        lastUpdatedAt = now
                    )
                } else {
                    slot.copy(
                        hungerPercent = nextHunger,
                        happinessPercent = nextHappiness,
                        lastUpdatedAt = now
                    )
                }
            }
        }
        repository.savePetSlots(updatedSlots)
    }

    // Method to keep compile compatibility if other classes invoke 'tickGarden'
    suspend fun tickGarden(deltaSeconds: Long) {
        tickGame(deltaSeconds)
    }

    // --- Offline Time Calculation ---
    private suspend fun calculateOfflineGains() {
        val stats = repository.getStatsMap()
        val lastVisit = stats["last_visit_time"]?.toLongOrNull() ?: System.currentTimeMillis()
        val durationMillis = System.currentTimeMillis() - lastVisit
        val durationSeconds = durationMillis / 1000

        if (durationSeconds < 10) return // Skip tiny intervals

        // Capped at 12 hours to prevent starving
        val cappedSeconds = min(43200L, durationSeconds)

        val currentSlots = repository.getPetSlotsList()
        val now = System.currentTimeMillis()
        val completedExploreItems = mutableListOf<String>()
        var sparksEarned = 0

        val updatedSlots = currentSlots.map { slot ->
            if (slot.petTypeId == null) {
                slot
            } else {
                // Update hunger and happiness decay
                val decayRate = 1.0f / (8 * 3600f)
                val nextHunger = (slot.hungerPercent - decayRate * cappedSeconds).coerceIn(0.1f, 1.0f)

                val happyDecay = 1.0f / (12 * 3600f)
                val nextHappiness = (slot.happinessPercent - happyDecay * cappedSeconds).coerceIn(0.2f, 1.0f)

                // Check if exploration finished while offline
                if (slot.activityStatus == 1 && slot.activityEndTime > 0L && lastVisit + (cappedSeconds * 1000L) >= slot.activityEndTime) {
                    // Auto-resolve exploration completed offline to reward the player!
                    val petSpec = GardenSpecs.getPetById(slot.petTypeId)
                    val areaId = slot.activityAreaId ?: 1
                    val areaSpec = GardenSpecs.getAreaById(areaId) ?: GardenSpecs.areas[0]

                    // Pull random collection item belonging to this area or general
                    val potentialItems = GardenSpecs.collectionItems.filter { it.sourceAreaName.contains(areaSpec.emoji) || it.sourceAreaName.contains(areaSpec.name.substringBefore(" ")) || Math.random() < 0.4 }
                    val foundItem = if (potentialItems.isNotEmpty()) potentialItems.random() else GardenSpecs.collectionItems.random()

                    // Record item found in database
                    repository.addCollectionItem(foundItem.id)

                    // Adventure reward stats
                    val rewardSparks = 20 + (areaId * 15)
                    val rewardXp = 15 + (areaId * 10)
                    sparksEarned += rewardSparks

                    completedExploreItems.add("${petSpec?.name ?: "반려동물"}가 ${areaSpec.name.substringBefore(" ")}에서 '${foundItem.emoji} ${foundItem.name}' 을(를) 발견하고 무사히 복귀했습니다!")
                    
                    // Reset slot activity status to idle
                    slot.copy(
                        hungerPercent = nextHunger,
                        happinessPercent = nextHappiness,
                        activityStatus = 0, // Idle
                        activityEndTime = 0L,
                        activityAreaId = null,
                        lastUpdatedAt = now
                    )
                } else {
                    slot.copy(
                        hungerPercent = nextHunger,
                        happinessPercent = nextHappiness,
                        lastUpdatedAt = now
                    )
                }
            }
        }

        repository.savePetSlots(updatedSlots)

        if (sparksEarned > 0) {
            _dewCount.value += sparksEarned
            addKeeperXp(25 * completedExploreItems.size)
        }

        if (completedExploreItems.isNotEmpty() || durationSeconds > 120) {
            _offlineReward.value = OfflineRewardDetails(
                elapsedSeconds = durationSeconds,
                hungerDecreased = (cappedSeconds.toFloat() / 36000f).coerceIn(0.01f, 0.9f),
                adventuresCompleted = completedExploreItems,
                sparksGained = sparksEarned,
                showDialog = true
            )
        }

        saveStatsToDb()
    }

    fun dismissOfflineReward() {
        _offlineReward.value = null
    }

    fun dismissLevelUpCelebration() {
        _levelUpCelebration.value = null
    }

    fun dismissAdventureReveal() {
        _adventureCompletedReveal.value = null
    }

    // --- Pet Caring Actions ---

    // 1. Adopt Pet
    fun adoptPet(slotId: Int, petTypeId: Int, customName: String) = viewModelScope.launch {
        val spec = GardenSpecs.getPetById(petTypeId) ?: return@launch
        if (_dewCount.value < spec.adoptCost) return@launch // Insufficient funds

        _dewCount.value -= spec.adoptCost

        val currentSlots = repository.getPetSlotsList()
        val updatedSlots = currentSlots.map { slot ->
            if (slot.id == slotId) {
                PetSlotEntity(
                    id = slotId,
                    petTypeId = petTypeId,
                    petName = customName.ifBlank { spec.name },
                    growthPercent = 0.0f,
                    hungerPercent = 1.0f,
                    happinessPercent = 1.0f,
                    level = 1,
                    activityStatus = 0,
                    activityEndTime = 0L,
                    activityAreaId = null,
                    lastUpdatedAt = System.currentTimeMillis()
                )
            } else {
                slot
            }
        }
        repository.savePetSlots(updatedSlots)

        // Mark pet kind as discovered in Pet Dex
        repository.updatePetDex(petTypeId = petTypeId, unlocked = true, adoptCountIncrement = 1)
        saveStatsToDb()
        addKeeperXp(30)
    }

    // 2. Feed Pet
    fun feedPet(slotId: Int, foodTier: Int) = viewModelScope.launch {
        val currentSlots = repository.getPetSlotsList()
        val slot = currentSlots.find { it.id == slotId } ?: return@launch
        if (slot.petTypeId == null || slot.activityStatus == 1) return@launch // Can't feed empty or exploring pet

        val spec = GardenSpecs.getPetById(slot.petTypeId) ?: return@launch

        // Food types: 1 = Basic (Free), 2 = Premium Cookie (15 Sparks), 3 = Royal Honey/Cake (50 Sparks)
        val foodCost = when (foodTier) {
            1 -> 0
            2 -> 15
            3 -> 50
            else -> 0
        }

        if (_dewCount.value < foodCost) return@launch // No sparks
        _dewCount.value -= foodCost

        // Calculate hunger and growth additions
        val hungerAdd = when (foodTier) {
            1 -> 0.3f
            2 -> 0.6f
            3 -> 1.0f
            else -> 0.3f
        }

        // Base growth gained: preferred food gives +50% bonus!
        val preferredMultiplier = if (foodTier == 3 || (foodTier == 2 && Math.random() < 0.6f)) 1.5f else 1.0f
        val growthBase = when (foodTier) {
            1 -> 0.12f
            2 -> 0.28f
            3 -> 0.60f
            else -> 0.10f
        }
        val growthGained = growthBase * preferredMultiplier

        var petLevel = slot.level
        var nextGrowth = slot.growthPercent + growthGained
        var leveledUp = false

        if (nextGrowth >= 1.0f) {
            leveledUp = true
            nextGrowth -= 1.0f
            petLevel++
            addKeeperXp(50 + (petLevel * 15)) // Sparks and Keeper XP
        }

        // Trigger floating reward particle
        addNewParticle(slotId, "🎂")

        val updatedSlots = currentSlots.map { s ->
            if (s.id == slotId) {
                s.copy(
                    hungerPercent = (s.hungerPercent + hungerAdd).coerceAtMost(1.0f),
                    growthPercent = if (leveledUp) nextGrowth.coerceAtMost(0.99f) else nextGrowth.coerceAtMost(0.99f), // keep below 1.0f for display or trigger transition
                    level = petLevel,
                    lastUpdatedAt = System.currentTimeMillis()
                )
            } else {
                s
            }
        }
        repository.savePetSlots(updatedSlots)
        saveStatsToDb()
    }

    // 3. Play with Pet (Increases happiness, gives light growth boost)
    fun playWithPet(slotId: Int) = viewModelScope.launch {
        val currentSlots = repository.getPetSlotsList()
        val slot = currentSlots.find { it.id == slotId } ?: return@launch
        if (slot.petTypeId == null || slot.activityStatus == 1) return@launch

        val happyAdd = 0.3f + (_toyLevel.value - 1) * 0.05f
        val growthAdd = 0.04f + (_toyLevel.value - 1) * 0.01f

        var petLevel = slot.level
        var nextGrowth = slot.growthPercent + growthAdd
        var leveledUp = false

        if (nextGrowth >= 1.0f) {
            leveledUp = true
            nextGrowth -= 1.0f
            petLevel++
            addKeeperXp(50)
        }

        // Random fun playing emoticon particle
        val playEmojis = listOf("❤️", "🐾", "🎾", "✨", "🎵", "😻", "🌟")
        addNewParticle(slotId, playEmojis.random())

        val updatedSlots = currentSlots.map { s ->
            if (s.id == slotId) {
                s.copy(
                    happinessPercent = (s.happinessPercent + happyAdd).coerceAtMost(1.0f),
                    growthPercent = nextGrowth.coerceAtMost(0.99f),
                    level = petLevel,
                    lastUpdatedAt = System.currentTimeMillis()
                )
            } else {
                s
            }
        }
        repository.savePetSlots(updatedSlots)
        addKeeperXp(8)
        saveStatsToDb()
    }

    private fun addNewParticle(slotId: Int, emoji: String) {
        val currentNotes = _tapNotes.value.toMutableList()
        currentNotes.add(Pair(slotId, emoji))
        _tapNotes.value = currentNotes

        viewModelScope.launch {
            delay(1200L)
            val updated = _tapNotes.value.toMutableList()
            val match = updated.find { it.first == slotId && it.second == emoji }
            if (match != null) {
                updated.remove(match)
                _tapNotes.value = updated
            }
        }
    }

    // 4. Send Pet On Adventure
    fun sendPetOnAdventure(slotId: Int, areaId: Int) = viewModelScope.launch {
        val currentSlots = repository.getPetSlotsList()
        val slot = currentSlots.find { it.id == slotId } ?: return@launch
        if (slot.petTypeId == null || slot.activityStatus != 0) return@launch // Pet must be idle

        val areaSpec = GardenSpecs.getAreaById(areaId) ?: return@launch
        if (_gardenLevel.value < areaSpec.requiredLevel) return@launch // Requirements not met

        val now = System.currentTimeMillis()
        val endTime = now + (areaSpec.explorationSeconds * 1000L)

        val updatedSlots = currentSlots.map { s ->
            if (s.id == slotId) {
                s.copy(
                    activityStatus = 1, // Exploring
                    activityEndTime = endTime,
                    activityAreaId = areaId,
                    lastUpdatedAt = now
                )
            } else {
                s
            }
        }
        repository.savePetSlots(updatedSlots)
        saveStatsToDb()
    }

    // 5. Claim Adventure Rewards
    fun claimAdventureRewards(slotId: Int) = viewModelScope.launch {
        val currentSlots = repository.getPetSlotsList()
        val slot = currentSlots.find { it.id == slotId } ?: return@launch
        if (slot.petTypeId == null || slot.activityStatus != 1) return@launch

        val now = System.currentTimeMillis()
        if (now < slot.activityEndTime) return@launch // Not finished yet

        val petSpec = GardenSpecs.getPetById(slot.petTypeId) ?: return@launch
        val areaId = slot.activityAreaId ?: 1
        val areaSpec = GardenSpecs.getAreaById(areaId) ?: GardenSpecs.areas[0]

        // Find relevant collection items belonging to this area or general pool items
        val potentialItems = GardenSpecs.collectionItems.filter { it.sourceAreaName.contains(areaSpec.emoji) || it.sourceAreaName.contains(areaSpec.name.substringBefore(" ")) || Math.random() < 0.4 }
        val foundItem = if (potentialItems.isNotEmpty()) potentialItems.random() else GardenSpecs.collectionItems.random()

        // 1. Save discovered collection item in DB
        repository.addCollectionItem(foundItem.id)

        // 2. Award Spark currency
        val rewardSparks = 25 + (areaId * 15) + (Math.random() * 15).toInt()
        _dewCount.value += rewardSparks

        // 3. Award Keeper XP
        val rewardXp = 20 + (areaId * 10)
        addKeeperXp(rewardXp)

        // 4. Trigger celebratory full-screen chest dialog reveal!
        _adventureCompletedReveal.value = AdventureCompletedReveal(
            petName = slot.petName,
            petEmoji = petSpec.emoji,
            areaName = areaSpec.name.substringBefore(" ("),
            itemId = foundItem.id,
            itemName = foundItem.name,
            itemEmoji = foundItem.emoji,
            itemRarity = foundItem.rarity,
            itemLore = foundItem.lore,
            sparksReward = rewardSparks,
            keeperXpReward = rewardXp
        )

        // Reset the pet slot back to idle
        val updatedSlots = currentSlots.map { s ->
            if (s.id == slotId) {
                s.copy(
                    activityStatus = 0,
                    activityEndTime = 0L,
                    activityAreaId = null,
                    lastUpdatedAt = now
                )
            } else {
                s
            }
        }
        repository.savePetSlots(updatedSlots)
        saveStatsToDb()
    }

    private fun addKeeperXp(xpGained: Int) {
        val currentXp = _gardenXp.value + xpGained
        var level = _gardenLevel.value

        var req = getXpThreshold(level)
        var tempXp = currentXp

        while (tempXp >= req) {
            tempXp -= req
            level++
            _levelUpCelebration.value = level // triggers celebration
            req = getXpThreshold(level)
        }

        _gardenXp.value = tempXp
        _gardenLevel.value = level
    }

    fun getXpThreshold(level: Int): Int {
        return when (level) {
            1 -> 80
            2 -> 200
            3 -> 450
            4 -> 900
            5 -> 1800
            6 -> 3500
            7 -> 6000
            8 -> 10000
            else -> 18000
        }
    }

    // --- Basket slot unlocks ---
    fun getBasketSlotUnlockPrice(id: Int): Int {
        return when (id) {
            3 -> 200
            4 -> 800
            5 -> 3000
            else -> 0
        }
    }

    fun getBasketSlotUnlockLevelReq(id: Int): Int {
        return when (id) {
            3 -> 2 // Slot 3 opens at Keeper level 2
            4 -> 4 // Slot 4 opens at Keeper level 4
            5 -> 5 // Slot 5 opens at Keeper level 5
            else -> 1
        }
    }

    fun isSlotUnlocked(slotId: Int): Boolean {
        if (slotId in 0..2) return true
        if (slotId == 3) return _slotUnlocked3.value || _gardenLevel.value >= getBasketSlotUnlockLevelReq(3)
        if (slotId == 4) return _slotUnlocked4.value || _gardenLevel.value >= getBasketSlotUnlockLevelReq(4)
        if (slotId == 5) return _slotUnlocked5.value || _gardenLevel.value >= getBasketSlotUnlockLevelReq(5)
        return false
    }

    fun unlockSlotBySparks(slotId: Int) = viewModelScope.launch {
        if (isSlotUnlocked(slotId)) return@launch

        val price = getBasketSlotUnlockPrice(slotId)
        if (_dewCount.value < price) return@launch

        _dewCount.value -= price
        when (slotId) {
            3 -> _slotUnlocked3.value = true
            4 -> _slotUnlocked4.value = true
            5 -> _slotUnlocked5.value = true
        }
        saveStatsToDb()
    }

    // 6. Treat Upgrade
    fun getUpgradeCost(currentLvl: Int): Int {
        if (currentLvl >= 5) return Int.MAX_VALUE
        return (120 * 2.1.pow(currentLvl - 1)).toInt()
    }

    fun upgradeToy() = viewModelScope.launch {
        val cl = _toyLevel.value
        val cost = getUpgradeCost(cl)
        if (cl < 5 && _dewCount.value >= cost) {
            _dewCount.value -= cost
            _toyLevel.value = cl + 1
            saveStatsToDb()
        }
    }

    // Treat upgrade for backward-compatibility wrapper
    fun upgradeTool(type: String) = viewModelScope.launch {
        if (type == "music" || type == "watering_can" || type == "soil") {
            upgradeToy()
        }
    }

    fun claimFreeSparks() = viewModelScope.launch {
        _dewCount.value += 200
        saveStatsToDb()
    }
}
