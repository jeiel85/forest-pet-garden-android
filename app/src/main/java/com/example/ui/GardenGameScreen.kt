package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.GardenViewModel
import com.example.viewmodel.OfflineRewardDetails
import com.example.viewmodel.AdventureCompletedReveal

enum class GameTab(val title: String, val icon: String) {
    GARDEN("내 반려숲", "🏡"),
    DEX("포근 도감", "📔"),
    RELICS("놀이터", "🏺"),
    SETTINGS("정보", "⚙️")
}

enum class Weather(val label: String, val emoji: String, val brush: Brush) {
    SUNNY("따스한 햇살", "🌞", Brush.verticalGradient(listOf(WarmBeige, Color(0xFFFBEAD2)))),
    RAINY("촉촉한 봄비", "🌧️", Brush.verticalGradient(listOf(WarmBeige, Color(0xFFD6E4EB)))),
    WINDY("싱그러운 산들바람", "🍃", Brush.verticalGradient(listOf(WarmBeige, Color(0xFFE2EDDB))))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GardenGameScreen(
    viewModel: GardenViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(GameTab.GARDEN) }
    var activeWeather by remember { mutableStateOf(Weather.SUNNY) }

    // Observers from ViewModel
    val dewCount by viewModel.dewCount.collectAsStateWithLifecycle()
    val gardenLevel by viewModel.gardenLevel.collectAsStateWithLifecycle()
    val gardenXp by viewModel.gardenXp.collectAsStateWithLifecycle()
    val toyLevel by viewModel.toyLevel.collectAsStateWithLifecycle()
    val petSlots by viewModel.petSlots.collectAsStateWithLifecycle(initialValue = emptyList())
    val petDex by viewModel.petDex.collectAsStateWithLifecycle(initialValue = emptyList())
    val petCollections by viewModel.petCollections.collectAsStateWithLifecycle(initialValue = emptyList())
    val offlineReward by viewModel.offlineReward.collectAsStateWithLifecycle()
    val adventureCompletedReveal by viewModel.adventureCompletedReveal.collectAsStateWithLifecycle()
    val levelUpCelebration by viewModel.levelUpCelebration.collectAsStateWithLifecycle()

    var showAdoptSlotId by remember { mutableStateOf<Int?>(null) }
    var showAdventureSlotId by remember { mutableStateOf<Int?>(null) }
    var showInteractionSlotId by remember { mutableStateOf<Int?>(null) }

    val weatherBrush = activeWeather.brush

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        containerColor = WarmBeige
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(weatherBrush)
        ) {
            // Interactive atmosphere effects
            WeatherAtmosphereEffects(weather = activeWeather)

            // Canvas decorative glow effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.18f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SageGreen, Color.Transparent),
                            radius = 240.dp.toPx()
                        ),
                        radius = 240.dp.toPx()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Little Forest Keeper Status Header
                StatusHeader(
                    level = gardenLevel,
                    xp = gardenXp,
                    maxXp = viewModel.getXpThreshold(gardenLevel),
                    sparks = dewCount,
                    weatherState = activeWeather,
                    onWeatherChange = { activeWeather = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1.0f)) {
                    when (activeTab) {
                        GameTab.GARDEN -> {
                            GardenPlaygroundView(
                                slots = petSlots,
                                viewModel = viewModel,
                                onOpenAdopt = { slotId -> showAdoptSlotId = slotId },
                                onOpenAdventure = { slotId -> showAdventureSlotId = slotId },
                                onOpenInteraction = { slotId -> showInteractionSlotId = slotId }
                            )
                        }
                        GameTab.DEX -> {
                            PetDexAndCollectionsView(
                                dexList = petDex,
                                foundItemsList = petCollections
                            )
                        }
                        GameTab.RELICS -> {
                            RelicsPlaygroundView(
                                viewModel = viewModel,
                                sparksCount = dewCount,
                                toyLvl = toyLevel,
                                slots = petSlots
                            )
                        }
                        GameTab.SETTINGS -> {
                            SettingsGuideView(
                                level = gardenLevel,
                                sparksCount = dewCount,
                                slotsCount = petSlots.filter { it.petTypeId != null }.size
                            )
                        }
                    }
                }
            }

            // --- Floating & Active Particle Overlays ---
            val tapParticles by viewModel.tapNotes.collectAsStateWithLifecycle()
            tapParticles.forEach { particle ->
                FloatingParticleEmoji(slotId = particle.first, emoji = particle.second)
            }

            // --- Interactive Dialog Sheets ---

            // 1. Adoption Dialog
            showAdoptSlotId?.let { slotId ->
                AdoptPetSelectorDialog(
                    mySparks = dewCount,
                    onDismiss = { showAdoptSlotId = null },
                    onAdopt = { petTypeId, customName ->
                        viewModel.adoptPet(slotId, petTypeId, customName)
                        showAdoptSlotId = null
                    }
                )
            }

            // 2. Adventure Area Selector Sheet
            showAdventureSlotId?.let { slotId ->
                AdventureAreaSelectorDialog(
                    userLevel = gardenLevel,
                    onDismiss = { showAdventureSlotId = null },
                    onChooseArea = { areaId ->
                        viewModel.sendPetOnAdventure(slotId, areaId)
                        showAdventureSlotId = null
                    }
                )
            }

            // 3. Adventure Chest Reward Reveal Overlay
            adventureCompletedReveal?.let { reveal ->
                AdventureChestRevealOverlay(
                    reveal = reveal,
                    onDismiss = { viewModel.dismissAdventureReveal() }
                )
            }

            // 4. Offline Gains Summary dialogue
            offlineReward?.let { reward ->
                OfflineGainsSummaryDialog(
                    reward = reward,
                    onDismiss = { viewModel.dismissOfflineReward() }
                )
            }

            // 5. Level-up Celebration dialog
            levelUpCelebration?.let { nextLevel ->
                KeeperLevelUpCelebrationDialog(
                    level = nextLevel,
                    onDismiss = { viewModel.dismissLevelUpCelebration() }
                )
            }

            // 6. Immersive Portrait Interactive Pet feeding & petting dialog
            showInteractionSlotId?.let { slotId ->
                val activeSlot = petSlots.find { it.id == slotId }
                if (activeSlot != null && activeSlot.petTypeId != null) {
                    InteractivePetCareDialog(
                        slot = activeSlot,
                        viewModel = viewModel,
                        mySparks = dewCount,
                        onDismiss = { showInteractionSlotId = null }
                    )
                }
            }
        }
    }
}

// --- Status Header ---
@Composable
fun StatusHeader(
    level: Int,
    xp: Int,
    maxXp: Int,
    sparks: Int,
    weatherState: Weather,
    onWeatherChange: (Weather) -> Unit
) {
    val xpProgress = (xp.toFloat() / maxXp.toFloat()).coerceIn(0.0f, 1.0f)
    val animatedXpProgress by animateFloatAsState(targetValue = xpProgress, animationSpec = tween(500))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CreamIvory.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, LineBeige),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Little Forest Keeper credentials
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SageGreen)
                            .border(1.5.dp, ForestGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌲", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Little Forest Keeper",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = "Lv. $level • 숲지기 보초",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMocha.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Sparks Token counter
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = BoxBeige),
                    border = BorderStroke(1.dp, LineBeige),
                    modifier = Modifier.shadow(1.dp, RoundedCornerShape(50))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$sparks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeachAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Realtime Progress to path
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { animatedXpProgress },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ForestGreen,
                    trackColor = BoxBeige
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$xp / $maxXp XP",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMocha
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LineBeige.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Weather conditions toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "정원 날씨 변경:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMocha.copy(alpha = 0.8f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Weather.values().forEach { weather ->
                        val isSelected = weather == weatherState
                        val buttonColor = if (isSelected) SageGreen else Color.Transparent
                        val textColor = if (isSelected) DarkSlate else DeepMocha.copy(alpha = 0.6f)
                        val borderStroke = if (isSelected) BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)) else null

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(buttonColor)
                                .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(10.dp)) else Modifier)
                                .clickable { onWeatherChange(weather) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = weather.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = weather.label.substring(4), // "햇살", "봄비", "산들바람"
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Main Garden View containing empty baskets & active cozy pets ---
@Composable
fun GardenPlaygroundView(
    slots: List<PetSlotEntity>,
    viewModel: GardenViewModel,
    onOpenAdopt: (Int) -> Unit,
    onOpenAdventure: (Int) -> Unit,
    onOpenInteraction: (Int) -> Unit
) {
    if (slots.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ForestGreen)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(slots) { slot ->
                PetBasketCard(
                    slot = slot,
                    viewModel = viewModel,
                    isUnlocked = viewModel.isSlotUnlocked(slot.id),
                    onOpenAdopt = { onOpenAdopt(slot.id) },
                    onOpenAdventure = { onOpenAdventure(slot.id) },
                    onOpenInteraction = { onOpenInteraction(slot.id) }
                )
            }
        }
    }
}

// Individual Cozy Pet basket or sleeping slot
@Composable
fun PetBasketCard(
    slot: PetSlotEntity,
    viewModel: GardenViewModel,
    isUnlocked: Boolean,
    onOpenAdopt: () -> Unit,
    onOpenAdventure: () -> Unit,
    onOpenInteraction: () -> Unit
) {
    val scaleAnim = rememberInfiniteTransition(label = "pet_float")
    val floatOffset by scaleAnim.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    if (!isUnlocked) {
        // Locked slot card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .alpha(0.72f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = StarSilver.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, LineBeige)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🔒", fontSize = 22.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "숲지기 레벨 ${viewModel.getBasketSlotUnlockLevelReq(slot.id)} 도달 시 개방",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMocha.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { viewModel.unlockSlotBySparks(slot.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = PeachAmber),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "즉시 개방 ✨${viewModel.getBasketSlotUnlockPrice(slot.id)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    } else if (slot.petTypeId == null) {
        // Unlocked but Empty sleep basket card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CreamIvory.copy(alpha = 0.85f)),
            border = BorderStroke(1.5.dp, LineBeige.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BoxBeige),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧺", fontSize = 34.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "빈 바구니 보금자리",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = "바람이 솔솔 불어 고요한 둥지입니다.",
                            fontSize = 10.sp,
                            color = DeepMocha.copy(alpha = 0.6f)
                        )
                    }
                }

                Button(
                    onClick = { onOpenAdopt() },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "분양 받기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    } else {
        // Active Cozy Pet Card Representation!
        val petSpec = GardenSpecs.getPetById(slot.petTypeId)
        val isExploring = slot.activityStatus == 1
        val now = System.currentTimeMillis()
        val exploreActiveRemainingSecs = if (isExploring) {
            ((slot.activityEndTime - now) / 1000L).coerceAtLeast(0L)
        } else 0L

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CreamIvory),
            border = BorderStroke(1.dp, LineBeige)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pet Visual representation
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BoxBeige)
                        .clickable { onOpenInteraction() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = petSpec?.emoji ?: "🦊",
                            fontSize = 38.sp,
                            modifier = Modifier.scale(if (isExploring) 1.0f else floatOffset)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(ForestGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Lv.${slot.level}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (isExploring) {
                        // Sleeping/Exploring flag overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎒", fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Stats and control block
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = slot.petName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    Text(
                        text = petSpec?.breed ?: "숲속 동물 친구",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMocha.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress indicators: Contentedness & Fullness
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "성장", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeepMocha, modifier = Modifier.width(22.dp))
                        LinearProgressIndicator(
                            progress = { slot.growthPercent },
                            modifier = Modifier
                                .weight(1.0f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = ForestGreen,
                            trackColor = BoxBeige
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${(slot.growthPercent * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "배부름", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeepMocha, modifier = Modifier.width(22.dp))
                        LinearProgressIndicator(
                            progress = { slot.hungerPercent },
                            modifier = Modifier
                                .weight(1.0f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = SoftSky,
                            trackColor = BoxBeige
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${(slot.hungerPercent * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "행복도", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeepMocha, modifier = Modifier.width(22.dp))
                        LinearProgressIndicator(
                            progress = { slot.happinessPercent },
                            modifier = Modifier
                                .weight(1.0f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PeachAmber,
                            trackColor = BoxBeige
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${(slot.happinessPercent * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons/States
                    if (isExploring) {
                        val isDone = exploreActiveRemainingSecs <= 0L
                        val btnColor = if (isDone) PeachAmber else BoxBeige
                        val btnText = if (isDone) "탐험 완료 🎁" else "${exploreActiveRemainingSecs}초 남음..."
                        val textCol = if (isDone) Color.White else DeepMocha

                        Button(
                            onClick = { if (isDone) viewModel.claimAdventureRewards(slot.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        ) {
                            Text(text = btnText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textCol)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Interact & Care
                            Button(
                                onClick = { onOpenInteraction() },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(28.dp)
                            ) {
                                Text(text = "💕 교감 & 돌보기", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            }

                            // Explore
                            Button(
                                onClick = { onOpenAdventure() },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(28.dp)
                            ) {
                                Text(text = "🌿 탐험보내기", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Pet Dex & Collections Journal View ---
@Composable
fun PetDexAndCollectionsView(
    dexList: List<PetDexEntity>,
    foundItemsList: List<PetCollectionEntity>
) {
    var subTabState by remember { mutableStateOf(0) } // 0 = Pets, 1 = Collections
    var selectedPetSpec by remember { mutableStateOf<PetSpec?>(null) }
    var selectedTreasureSpec by remember { mutableStateOf<CollectionItemSpec?>(null) }

    val discoveredPetsCount = GardenSpecs.pets.count { petSpec ->
        dexList.any { it.petTypeId == petSpec.id && it.unlocked }
    }
    val foundTreasuresCount = GardenSpecs.collectionItems.count { itemSpec ->
        foundItemsList.any { it.itemId == itemSpec.id && it.foundCount > 0 }
    }
    val totalDiscovered = discoveredPetsCount + foundTreasuresCount
    val totalProgressPercent = totalDiscovered.toFloat() / 20f

    val badgeName = when {
        totalProgressPercent >= 1.0f -> "🏆 대자연의 전설 마스터"
        totalProgressPercent >= 0.8f -> "👑 신성한 반려숲의 수호자"
        totalProgressPercent >= 0.5f -> "🧭 노련한 정원 탐험대장"
        totalProgressPercent >= 0.2f -> "🌿 다정한 바람 소수지기"
        else -> "🌱 갓 피어난 새내기 숲지기"
    }

    val badgeColor = when {
        totalProgressPercent >= 0.8f -> PeachAmber
        totalProgressPercent >= 0.5f -> ForestGreen
        totalProgressPercent >= 0.2f -> MeadowGrass
        else -> CozyClay
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Mastery Progress Summary Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CreamIvory),
            border = BorderStroke(1.dp, LineBeige)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏆", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "반려숲 수집 도감첩",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                        }
                        Text(
                            text = "숲을 탐험하고 속삭임을 수집하세요",
                            fontSize = 9.sp,
                            color = DeepMocha.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeName,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: Pets progress bar
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(BoxBeige, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🦊", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "반려동물", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                            }
                            Text(text = "$discoveredPetsCount / 8", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ForestGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { discoveredPetsCount.toFloat() / 8f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ForestGreen,
                            trackColor = LineBeige
                        )
                    }

                    // Right: Treasures progress bar
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(BoxBeige, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💎", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "발견한 보물", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                            }
                            Text(text = "$foundTreasuresCount / 12", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = PeachAmber)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { foundTreasuresCount.toFloat() / 12f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PeachAmber,
                            trackColor = LineBeige
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Combined Progress Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "전체 도감 완성률: ",
                        fontSize = 10.sp,
                        color = DeepMocha
                    )
                    Text(
                        text = "${(totalProgressPercent * 100).toInt()}% 완료",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    Text(
                        text = " (${totalDiscovered}/20 수집)",
                        fontSize = 9.sp,
                        color = DeepMocha.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Subtabs (Pets vs Treasures)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { subTabState = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTabState == 0) ForestGreen else BoxBeige
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1.0f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "동물 도감 🦊 ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (subTabState == 0) Color.White else DeepMocha
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (subTabState == 0) Color.White.copy(alpha = 0.25f) else LineBeige,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$discoveredPetsCount",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subTabState == 0) Color.White else DarkSlate
                        )
                    }
                }
            }

            Button(
                onClick = { subTabState = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTabState == 1) ForestGreen else BoxBeige
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1.0f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "보물 백과 💎 ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (subTabState == 1) Color.White else DeepMocha
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (subTabState == 1) Color.White.copy(alpha = 0.25f) else LineBeige,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$foundTreasuresCount",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subTabState == 1) Color.White else DarkSlate
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (subTabState == 0) {
            // PET COLLECTION GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(GardenSpecs.pets) { petSpec ->
                    val dexMatch = dexList.find { it.petTypeId == petSpec.id }
                    val isDiscovered = dexMatch?.unlocked == true
                    val rarityColor = getRarityAccentColor(petSpec.rarity)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPetSpec = petSpec },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDiscovered) CreamIvory else StarSilver.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(
                            width = if (isDiscovered) 1.5.dp else 1.dp,
                            color = if (isDiscovered) rarityColor.copy(alpha = 0.7f) else LineBeige.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Pet Icon Area
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isDiscovered) SageGreen.copy(alpha = 0.4f) else BoxBeige),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDiscovered) {
                                    Text(text = petSpec.emoji, fontSize = 42.sp)
                                } else {
                                    Text(text = "❓", fontSize = 24.sp, color = DeepMocha.copy(alpha = 0.3f))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Name
                            Text(
                                text = if (isDiscovered) petSpec.name else "???",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDiscovered) DarkSlate else DeepMocha.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Breed
                            Text(
                                text = if (isDiscovered) petSpec.breed else "미발견 생명체",
                                fontSize = 8.sp,
                                color = if (isDiscovered) DeepMocha.copy(alpha = 0.7f) else DeepMocha.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Rarity pill or locked clue
                            if (isDiscovered) {
                                Box(
                                    modifier = Modifier
                                        .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = petSpec.rarity.label,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = rarityColor
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(StarSilver.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Sparks 주어 입양",
                                        fontSize = 7.sp,
                                        color = DeepMocha.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TREASURE COLLECTION GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(GardenSpecs.collectionItems) { itemSpec ->
                    val countEntity = foundItemsList.find { it.itemId == itemSpec.id }
                    val timesFound = countEntity?.foundCount ?: 0
                    val hasFound = timesFound > 0
                    val rarityColor = getRarityAccentColor(itemSpec.rarity)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTreasureSpec = itemSpec },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasFound) CreamIvory else StarSilver.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(
                            width = if (hasFound) 1.5.dp else 1.dp,
                            color = if (hasFound) rarityColor.copy(alpha = 0.7f) else LineBeige.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Treasure Icon
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (hasFound) SageGreen.copy(alpha = 0.4f) else BoxBeige),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasFound) {
                                    Text(text = itemSpec.emoji, fontSize = 42.sp)
                                } else {
                                    Text(text = "🔒", fontSize = 24.sp, color = DeepMocha.copy(alpha = 0.3f))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Name
                            Text(
                                text = if (hasFound) itemSpec.name else "고대의 신비",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasFound) DarkSlate else DeepMocha.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Source
                            Text(
                                text = if (hasFound) "탐험: ${itemSpec.sourceAreaName}" else "탐색처: ${itemSpec.sourceAreaName}",
                                fontSize = 8.sp,
                                color = if (hasFound) DeepMocha.copy(alpha = 0.7f) else DeepMocha.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Unlocked count or locked
                            if (hasFound) {
                                Box(
                                    modifier = Modifier
                                        .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${timesFound}개 발견됨 🏆",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = rarityColor
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(StarSilver.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "미발견 보물",
                                        fontSize = 7.sp,
                                        color = DeepMocha.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Popups
    selectedPetSpec?.let { spec ->
        val dexMatch = dexList.find { it.petTypeId == spec.id }
        val isUnlocked = dexMatch?.unlocked == true
        PetDexDetailDialog(
            petSpec = spec,
            isUnlocked = isUnlocked,
            onDismiss = { selectedPetSpec = null }
        )
    }

    selectedTreasureSpec?.let { spec ->
        val countEntity = foundItemsList.find { it.itemId == spec.id }
        val timesFound = countEntity?.foundCount ?: 0
        val hasFound = timesFound > 0
        val discoveredTime = countEntity?.discoveredTime ?: 0L
        TreasureDexDetailDialog(
            itemSpec = spec,
            isFound = hasFound,
            foundCount = timesFound,
            discoveredTime = discoveredTime,
            onDismiss = { selectedTreasureSpec = null }
        )
    }
}

@Composable
fun getRarityAccentColor(rarity: Rarity): Color {
    return when (rarity) {
        Rarity.COMMON -> CozyClay
        Rarity.UNCOMMON -> ForestGreen
        Rarity.RARE -> SoftSky
        Rarity.EPIC -> DreamViolet
        Rarity.LEGENDARY -> PeachAmber
    }
}

@Composable
fun PetDexDetailDialog(
    petSpec: PetSpec,
    isUnlocked: Boolean,
    onDismiss: () -> Unit
) {
    val rarityColor = getRarityAccentColor(petSpec.rarity)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Window Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "동물 기록 연구소", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    }
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(26.dp)
                            .background(BoxBeige, CircleShape)
                    ) {
                        Text(text = "✕", fontSize = 10.sp, color = DeepMocha, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Large Avatar Round Card
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) SageGreen.copy(alpha = 0.5f) else BoxBeige)
                        .border(2.dp, if (isUnlocked) rarityColor else LineBeige, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(text = petSpec.emoji, fontSize = 64.sp)
                    } else {
                        Text(text = "❓", fontSize = 48.sp, color = DeepMocha.copy(alpha = 0.3f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title, Name & Breed
                Text(
                    text = if (isUnlocked) petSpec.name else "수집되지 않은 신비로운 반려동물",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlate,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isUnlocked) "품종: ${petSpec.breed}" else "품종: 비밀",
                    fontSize = 10.sp,
                    color = DeepMocha.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rarity badge
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, rarityColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${petSpec.rarity.label} 레벨",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = rarityColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(BoxBeige, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "입양 Sparks: ✨${petSpec.adoptCost}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeachAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lore or Lock Clue
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BoxBeige),
                    border = BorderStroke(1.dp, LineBeige)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📝 숲의 연구 일지",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMocha
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUnlocked) petSpec.lore else "숲 속 비밀 자작나무 군락 아래에서 종종 기척이 느껴지나 아직 입양되지 않아 다정이 쌓이지 않았습니다.",
                            fontSize = 11.sp,
                            color = DarkSlate,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Preferences details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamIvory),
                        border = BorderStroke(1.dp, LineBeige)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ 육성 가이드 침구 & 교감 선호물",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🍎", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "가장 좋아하는 간식", fontSize = 9.sp, color = DeepMocha)
                                }
                                Text(text = petSpec.preferredFood, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🎾", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "반기는 장난감/소통", fontSize = 9.sp, color = DeepMocha)
                                }
                                Text(text = petSpec.preferredToy, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Locked instructions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SageGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡Hint:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "메인 정원 탭의 빈 요람 슬롯을 터치하고, ✨${petSpec.adoptCost} Sparks를 소모하여 아기동물로 입양하면 도감이 즉시 해제됩니다!",
                            fontSize = 8.sp,
                            color = DeepMocha
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "도감첩으로 돌아가기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

@Composable
fun TreasureDexDetailDialog(
    itemSpec: CollectionItemSpec,
    isFound: Boolean,
    foundCount: Int,
    discoveredTime: Long,
    onDismiss: () -> Unit
) {
    val rarityColor = getRarityAccentColor(itemSpec.rarity)
    val formattedDate = remember(discoveredTime) {
        if (discoveredTime > 0L) {
            try {
                java.text.SimpleDateFormat(
                    "yyyy년 MM월 dd일 HH시 mm분",
                    java.util.Locale.KOREAN
                ).format(java.util.Date(discoveredTime))
            } catch (e: Exception) {
                "최근 과거"
            }
        } else {
            "기록 없음"
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Window Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💎", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "숲속 고대 유물 분석소", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    }
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(26.dp)
                            .background(BoxBeige, CircleShape)
                    ) {
                        Text(text = "✕", fontSize = 10.sp, color = DeepMocha, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Large Avatar Round Card
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (isFound) SageGreen.copy(alpha = 0.5f) else BoxBeige)
                        .border(2.dp, if (isFound) rarityColor else LineBeige, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFound) {
                        Text(text = itemSpec.emoji, fontSize = 64.sp)
                    } else {
                        Text(text = "🔒", fontSize = 48.sp, color = DeepMocha.copy(alpha = 0.3f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title, Name & Breed
                Text(
                    text = if (isFound) itemSpec.name else "발견되지 않은 태고의 유물",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlate,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isFound) "발견 구역: ${itemSpec.sourceAreaName}" else "서식지: ${itemSpec.sourceAreaName}",
                    fontSize = 10.sp,
                    color = DeepMocha.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rarity badge
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, rarityColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${itemSpec.rarity.label} 유물",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = rarityColor
                        )
                    }
                    if (isFound) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(BoxBeige, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "발견 횟수: ${foundCount}회",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lore or Lock Clue
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BoxBeige),
                    border = BorderStroke(1.dp, LineBeige)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📜 고고학 관찰 일기",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMocha
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFound) itemSpec.lore else "고대 지도 조각에는 '${itemSpec.sourceAreaName}'의 무덤 혹은 연못 깊숙한 수풀 속에 이 보물이 신비로운 기운을 띈 채 자취를 감추고 있다고 전해집니다.",
                            fontSize = 11.sp,
                            color = DarkSlate,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (isFound) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Location Discovery Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamIvory),
                        border = BorderStroke(1.dp, LineBeige)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⏱️ 최초 발견 기록 타임스탬프",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PeachAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Locked instructions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SageGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡Hint:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "반려동물을 '${itemSpec.sourceAreaName}' 구역으로 소풍(탐험)보내세요! 무사히 복귀 시 기쁜 울음소리와 함께 일정 확률로 물어옵니다.",
                            fontSize = 8.sp,
                            color = DeepMocha
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "도감첩으로 돌아가기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

// --- Toys & Baskets upgrades (Relics) ---
@Composable
fun RelicsPlaygroundView(
    viewModel: GardenViewModel,
    sparksCount: Int,
    toyLvl: Int,
    slots: List<PetSlotEntity>
) {
    val upgradePrice = viewModel.getUpgradeCost(toyLvl)
    val isToyMax = toyLvl >= 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CreamIvory),
            border = BorderStroke(1.dp, LineBeige)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🧸 숲속 장난감 상자 업그레이드", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "어루만지고 놀 때 쓰다듬어주는 빗과 놀이 도구를 개량합니다. 레벨이 올라갈수록 반려동물들의 행복도와 친화 성장이 대폭 증가합니다.",
                    fontSize = 11.sp,
                    color = DeepMocha.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "장난감 박스 등급", fontSize = 11.sp, color = DeepMocha)
                        Text(text = "현재 Level $toyLvl / 5", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    }

                    if (isToyMax) {
                        Box(
                            modifier = Modifier
                                .background(SageGreen, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = "최대 레벨 도달 ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.upgradeToy() },
                            colors = ButtonDefaults.buttonColors(containerColor = PeachAmber),
                            shape = RoundedCornerShape(12.dp),
                            enabled = sparksCount >= upgradePrice
                        ) {
                            Text(text = "강화 ✨$upgradePrice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Adorable Sparks Fountain block guaranteeing no-IAP non-punitive layout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BoxBeige),
            border = BorderStroke(1.dp, LineBeige)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "💖 평온의 우물터 (무료 지원금)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "저희 숲속 정원은 과금을 강요하거나 동물 친구들을 영악하게 방치하지 않는 따뜻하고 지극히 순수한 게임입니다. 힘든 보모 파수꾼을 위해 무조건적인 Sparks 지원을 지원합니다.",
                    fontSize = 11.sp,
                    color = DeepMocha.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.claimFreeSparks() },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "포근 이슬 Sparks 가득 모금 (+✨200 받기)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- Information & Guides Setting Screen ---
@Composable
fun SettingsGuideView(
    level: Int,
    sparksCount: Int,
    slotsCount: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamIvory),
                border = BorderStroke(1.dp, LineBeige)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📖 숲지기 영주 길잡이 (Guide)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. 입양과 돌보기: 빈 요람을 터치하여 마음에 드는 아기동물을 Sparks로 입양하세요.\n" +
                               "2. 사과 피딩과 놀이: 먹을 것을 주면 배부름(Fullness)이 차오르며, 놀아주면 행복도와 함께 성장률이 가파르게 상승합니다.\n" +
                               "3. 탐험 보내기: 친구를 소나무 숲속이나 웅덩이로 탐험 보내면, 시간 내에 유익한 보물 무늬 조각을 입에 물고 무사히 복귀하여 Sparks와 Keeper XP를 받습니다.\n" +
                               "4. 보물 수집: 도감에서 내가 찾은 유물들의 전설적인 귀여운 스토리를 모두 해제해 보세요!",
                        fontSize = 11.sp,
                        color = DeepMocha.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamIvory),
                border = BorderStroke(1.dp, LineBeige)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "🌿 인디 개발자의 따뜻한 편지", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "이 게임은 당신의 지친 도심속 일상에 보송함을 선물해 주고자 기획된 가상 펫 육성 가든 게임입니다. 인앱 구매를 포함한 그 어떤 유료 결제도 필요 없이, 오프라인으로 머리를 쓰다듬으며 숲빛 꿈길을 만끽하세요. 늘 평온하시길 진심으로 기도합니다. 🌲💚",
                        fontSize = 10.sp,
                        color = DeepMocha.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// Custom BottomNavigationBar
@Composable
fun BottomNavigationBar(
    selectedTab: GameTab,
    onTabSelected: (GameTab) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        border = BorderStroke(1.dp, LineBeige),
        colors = CardDefaults.cardColors(containerColor = CreamIvory),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val buttonColor = if (isSelected) SageGreen else Color.Transparent
                val activePillModifier = if (isSelected) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, ForestGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                } else Modifier

                Column(
                    modifier = Modifier
                        .then(activePillModifier)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = tab.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DarkSlate else DeepMocha.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// --- Visual Effect particle overlays ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingParticleEmoji(
    slotId: Int,
    emoji: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "yOffset"
    )

    val fadeOut by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "fade"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {} // non-interactive
    ) {
        val lateralOffset = (slotId * 45) + 30 // jitter particles based on slot ID
        Box(
            modifier = Modifier
                .offset(x = lateralOffset.dp, y = (320 + floatOffset).dp)
                .alpha(fadeOut),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
        }
    }
}

// Falling weather background particle decorations
@Composable
fun WeatherAtmosphereEffects(weather: Weather) {
    if (weather == Weather.SUNNY) return

    val infiniteTransition = rememberInfiniteTransition(label = "weather")
    val positionOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weather_fall"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val count = 25
        for (i in 0 until count) {
            val startX = (i * 18).dp.toPx()
            val startY = ((startX + positionOffset.dp.toPx()) % size.height)

            if (weather == Weather.RAINY) {
                // Slanted rain lines
                drawLine(
                    color = SoftSky.copy(alpha = 0.5f),
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(startX - 8.dp.toPx(), startY + 15.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            } else if (weather == Weather.WINDY) {
                // Small rotating leaves or circles
                drawCircle(
                    color = MeadowGrass.copy(alpha = 0.35f),
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(startX + (startY % 40), startY)
                )
            }
        }
    }
}

// --- Custom Adorable Dialog Boxes ---

// 1. Adoption Dialog
@Composable
fun AdoptPetSelectorDialog(
    mySparks: Int,
    onDismiss: () -> Unit,
    onAdopt: (petTypeId: Int, name: String) -> Unit
) {
    var selectedPetId by remember { mutableStateOf(1) }
    var petCustomName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "🦊 보송동물 요람 분양받기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
        },
        text = {
            Column {
                Text(text = "바람 소리가 포근한 빈 둥지에 입양할 아기를 고르고, 사랑스러운 이름을 지어 터치하세요.", fontSize = 11.sp, color = DeepMocha)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = petCustomName,
                    onValueChange = { petCustomName = it },
                    label = { Text("동물 친구의 애칭 이름 지어주기", fontSize = 11.sp) },
                    placeholder = { Text("안 지으면 품종 이름이 됩니다", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = LineBeige
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GardenSpecs.pets) { pet ->
                        val isSelected = pet.id == selectedPetId
                        val isAffordable = mySparks >= pet.adoptCost
                        val containerCol = if (isSelected) SageGreen else BoxBeige
                        val borderCol = if (isSelected) ForestGreen else LineBeige

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPetId = pet.id },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = containerCol),
                            border = BorderStroke(1.5.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = pet.emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(text = "${pet.name} (${pet.breed})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                    Text(text = pet.lore, fontSize = 9.sp, color = DeepMocha.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (isAffordable) ForestGreen else Color.LightGray, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "✨${pet.adoptCost}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val finalChoose = GardenSpecs.getPetById(selectedPetId)
            val canAdopt = finalChoose != null && mySparks >= finalChoose.adoptCost

            Button(
                onClick = { onAdopt(selectedPetId, petCustomName) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = canAdopt
                    ) {
                Text(text = "품 고 안아주기 💖", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "취소", fontSize = 11.sp, color = DeepMocha)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

// 2. Adventure Area Selector Dialog
@Composable
fun AdventureAreaSelectorDialog(
    userLevel: Int,
    onDismiss: () -> Unit,
    onChooseArea: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "🌿 숲속 탐험 소풍 보내기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
        },
        text = {
            Column {
                Text(text = "반려동물을 숲속 미지의 영역으로 소풍 탐험 보냅니다. 안전한 소풍 후 예쁜 소집 보물 조각을 물고 돌아옵니다.", fontSize = 11.sp, color = DeepMocha)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.height(250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GardenSpecs.areas) { area ->
                        val isUnlocked = userLevel >= area.requiredLevel
                        val containerCol = if (isUnlocked) BoxBeige else StarSilver.copy(alpha = 0.3f)
                        val borderCol = if (isUnlocked) LineBeige else Color.Transparent

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isUnlocked) Modifier.clickable { onChooseArea(area.id) } else Modifier),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = containerCol),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (isUnlocked) area.emoji else "🔒", fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = area.name.substringBefore(" ("),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) DarkSlate else Color.Gray
                                    )
                                    Text(text = if (isUnlocked) area.desc else "숲지기 레벨 ${area.requiredLevel} 필요", fontSize = 9.sp, color = DeepMocha.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .background(SageGreen, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "${area.explorationSeconds}초", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "닫기", fontSize = 11.sp, color = DeepMocha)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

// 3. Adventure Chest Reveal Overlay Dialog with cute M3 graphic animation layout
@Composable
fun AdventureChestRevealOverlay(
    reveal: AdventureCompletedReveal,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉 탐험 복귀 대성공!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${reveal.petEmoji} ${reveal.petName}의 고요한 선물상자", fontSize = 11.sp, color = DeepMocha)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(SageGreen)
                        .border(2.dp, ForestGreen, RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = reveal.itemEmoji, fontSize = 54.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .background(PeachAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "희귀도: ${reveal.itemRarity.label}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeachAmber
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = reveal.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = reveal.itemLore,
                    fontSize = 10.sp,
                    color = DeepMocha.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = LineBeige.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "🎁 숲속 수령 보상:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "✨ Sparks +${reveal.sparksReward}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PeachAmber)
                    Text(text = "🌲 XP +${reveal.keeperXpReward}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "도감 보관함에 넣기 ❤️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(26.dp),
        containerColor = WarmBeige
    )
}

// 4. Offline Gains dialog
@Composable
fun OfflineGainsSummaryDialog(
    reward: OfflineRewardDetails,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "💤 포근 안식 슬립 정산", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
        },
        text = {
            Column {
                Text(
                    text = "당신이 따뜻한 잠을 청하는 동안 숲속 정원의 시간도 부드럽게 흘렀습니다.",
                    fontSize = 11.sp,
                    color = DeepMocha.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BoxBeige)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "• 흘러간 시간: ${reward.elapsedSeconds / 60}분 경과", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                        Text(text = "• 반려동물 배고픔: 약간 감소 줄임", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepMocha)
                        if (reward.sparksGained > 0) {
                            Text(text = "• 자동 탐험 Sparks 수거: ✨${reward.sparksGained} Sparks 가득!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PeachAmber)
                        }
                    }
                }

                if (reward.adventuresCompleted.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "📑 탐험 및 행동 일지:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier.height(80.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(reward.adventuresCompleted) { log ->
                            Text(text = "• $log", fontSize = 9.sp, color = DeepMocha)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "다정한 안부 나누기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

// 5. Keeper Level-up Celebration dialog
@Composable
fun KeeperLevelUpCelebrationDialog(
    level: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🔔 숲지기 영주 등급 상승!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Keeper Level Up!", fontSize = 11.sp, color = DeepMocha)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(SageGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "축하합니다! 정원 영지 숲지기 등급이\n[ 레벨 $level ] 로 승급하였습니다!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "새로운 숲길 탐험 영역과 빈 슬립 바구니 잠금 해제 조건들이 대폭 열렸습니다. 정원을 계속 번영시켜 한층 더 보송한 친구들을 영입하세요!",
                    fontSize = 10.sp,
                    color = DeepMocha.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "신나는 숲길 구경가기 ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

@Composable
fun InteractivePetCareDialog(
    slot: PetSlotEntity,
    viewModel: GardenViewModel,
    mySparks: Int,
    onDismiss: () -> Unit
) {
    val petSpec = GardenSpecs.getPetById(slot.petTypeId ?: 1) ?: return
    var scaleToggle by remember { mutableStateOf(false) }

    // Bounce spring animation when petted
    val animatedScale by animateFloatAsState(
        targetValue = if (scaleToggle) 1.18f else 1.0f,
        animationSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = {
            scaleToggle = false
        },
        label = "bounce"
    )

    // Dynamic cute messaging bubble state
    var dialogueLine by remember(slot.id, slot.petTypeId) {
        mutableStateOf("제 머리를 보송보송 쓰다듬거나 맛있는 숲속 간식을 먹여주세요! 💕")
    }

    val petPhrases = when (slot.petTypeId) {
        1 -> listOf(
            "손길이 닿으면 귀가 보송보송 쫑긋해져요! 숲지기님 최고! 💕",
            "아작 아작 사과를 먹으면 노을빛 꼬리를 살랑여요! 🍎",
            "쓰담쓰담해주실 때마다 마음속에 숲피크닉 온 것만 같아요!",
            "웅냥냥... 따스한 햇빛 냄새가 숲지기님 손에서 묻어나네요 🌲"
        )
        2 -> listOf(
            "음냐... 잠꼬대 아니에요... 숲지기님 품이 최고로 포근해... 💤",
            "유칼립 주스 한 모금이랑 따뜻한 손길가득! 🧉",
            "하프 바람 소리 흘려가며... 영구 안식 모드로 스르륵...",
            "코오오- 숲지기님의 지켜줌 아래서 둥둥 구름베개 꿈나라로..."
        )
        3 -> listOf(
            "귀가 쫑긋욕! 숲지기님이 쓰다듬어 주시면 기분 급상승! 🐰💞",
            "보송한 당근 한 조각, 오물오물... 숲 속 세상이 달콤해요!",
            "풀쩍 뛰어올라 숲지기님의 볼에 보송 숨결을 톡!",
            "산들바람 솜털 가닥가닥 다정이 충만한 기운이 풍깁니다!"
        )
        4 -> listOf(
            "도토리를 모으는 것보다 숲지기님과 안부 나누기가 백배 기뻐요! 🌰",
            "양 볼 가득 도토릭! 뽈뽈뽈 정원을 뛰놀 테야!",
            "머리가 보드라워져서 다람풍 날개가 펼쳐지는 느낌이에요!",
            "우드득! 견과류 한 자루 든든히 먹고 소풍 갈래요 ✨"
        )
        5 -> listOf(
            "조잘조잘! 이슬 물웅덩이에서 같이 물장구치실래요? ⛲",
            "아기 오리 비스킷 냠냠! 날개를 아장아장 흔들래요!",
            "숲지기님의 따뜻한 응원에 마음이 몽글몽글 구름빛이 됐어요 ☁️",
            "꽥꽥! 정원의 아침 이슬 지저귐이 아주 맑게 퍼집니다 ♪"
        )
        6 -> listOf(
            "꿀단지보다 숲지기님의 쓰다듬는 다정이 오백 상자 달콤해요! 🍯",
            "산딸기 산책 같이 가요! 웅냥냥 미소가 둥글 피어오릅니다.",
            "온 몸을 동글 흔들흔들! 든든하게 먹고 탐험할 준비 완료!",
            "꾸우우~ 정원의 등불 아래서 포근히 안아주시는 숲지기님!"
        )
        7 -> listOf(
            "눈안의 은하수가 숲지기님의 감촉에 별무리처럼 번져요... ⭐",
            "은하 촉촉 비스킷 냠! 꼬리 끝이 냥냥 밤빛으로 빛나네요 ฅ^•ﻌ•^ฅ",
            "골골골... 포근하고 영험한 숲기운 가득 채집 충전중... ❤️",
            "냥- 숲속 영지 가득 우주의 안녕과 신비로운 행운이 번져요"
        )
        8 -> listOf(
            "보름달 아래 속삭임... 지혜의 오랜 책장 속 보송한 행운을 드려요 📖",
            "청포도 젤리로 에너지가 부우 부우! 날개를 위엄 있게 펄럭!",
            "숲지기님의 손길은 숲 가든 전체에 우뚝 서는 평화의 뿌리랍니다.",
            "부우 부우~ 밤하늘 꿈길 속에서 보송한 미소를 드리옵니다."
        )
        else -> listOf(
            "머리를 쓰다듬어 주셔서 기뻐요! 💖",
            "냠냠, 맛있는 간식 고맙습니다! 🍀",
            "숲속 정원에서 정성껏 함께 성장해요!"
        )
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Title with Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💖", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${slot.petName}와(과) 교감하기",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                            Text(
                                text = "따스한 돌봄으로 행복지수를 채워주세요",
                                fontSize = 9.sp,
                                color = DeepMocha.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Simple Close icon button
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(26.dp)
                            .background(BoxBeige, CircleShape)
                    ) {
                        Text(text = "✕", fontSize = 10.sp, color = DeepMocha, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Speech bubble representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BoxBeige, RoundedCornerShape(12.dp))
                        .border(1.dp, LineBeige, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dialogueLine,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkSlate,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // INTERACTIVE TOUCH ZONE (Portrait-optimized large avatar)
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(SageGreen)
                        .border(2.dp, ForestGreen, CircleShape)
                        .clickable {
                            scaleToggle = true
                            dialogueLine = petPhrases.random()
                            viewModel.playWithPet(slot.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Soft background glow ring
                    Box(
                        modifier = Modifier
                            .size(115.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.scale(animatedScale)
                    ) {
                        Text(
                            text = petSpec.emoji,
                            fontSize = 62.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "머리 쓰담 💕",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sparks count indicator inside the dialog
                Row(
                    modifier = Modifier
                        .background(BoxBeige, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✨ 보유 Sparks: $mySparks", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PeachAmber)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Dashboard (Fills the center portion of portrait dialog)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamIvory),
                    border = BorderStroke(1.dp, LineBeige)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Level and Growth Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👑 성장 수치", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepMocha, modifier = Modifier.width(60.dp))
                            LinearProgressIndicator(
                                progress = { slot.growthPercent },
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ForestGreen,
                                trackColor = BoxBeige
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Lv.${slot.level} (${(slot.growthPercent * 100).toInt()}%)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Fullness Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🍎 배고픔 지수", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepMocha, modifier = Modifier.width(60.dp))
                            LinearProgressIndicator(
                                progress = { slot.hungerPercent },
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SoftSky,
                                trackColor = BoxBeige
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${(slot.hungerPercent * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Happiness Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💖 친밀도 수치", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepMocha, modifier = Modifier.width(60.dp))
                            LinearProgressIndicator(
                                progress = { slot.happinessPercent },
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PeachAmber,
                                trackColor = BoxBeige
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${(slot.happinessPercent * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FEEDING SHELF TITLE
                Text(text = "🍲 숲속 수제 영양 간식 밥상", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                Spacer(modifier = Modifier.height(4.dp))

                // Three customized interactive food buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Food Tier 1: Apple
                    FoodItemRow(
                        title = "이슬 촉촉 사과 🍎",
                        desc = "배부름 +30% | 성장률 보너스",
                        cost = "무료",
                        isAffordable = true,
                        onClick = {
                            if (slot.hungerPercent >= 0.99f) {
                                dialogueLine = "웅냥냥! 배가 아주 가득 불러요! 지금은 산책 소풍 준비를 할게요 🌿"
                            } else {
                                viewModel.feedPet(slot.id, 1)
                                dialogueLine = "아사삭! 꿀맛 사과네요! 배가 약간 차오릅니다 🍎"
                                scaleToggle = true
                            }
                        }
                    )

                    // Food Tier 2: Biscuit
                    val canAffordT2 = mySparks >= 15
                    FoodItemRow(
                        title = "달달 밤비스킷 🍪",
                        desc = "배부름 +60% | 선호보너스 확률 성장",
                        cost = "✨15",
                        isAffordable = canAffordT2,
                        onClick = {
                            if (slot.hungerPercent >= 0.99f) {
                                dialogueLine = "배가 아주 빵빵해서 움직이기 싫을 정도에요! ฅ^•ﻌ•^ฅ"
                            } else if (!canAffordT2) {
                                dialogueLine = "이슬 Sparks가 약간 모자라요... 아래 '평온의 우물터'에서 모금해 볼까요?"
                            } else {
                                viewModel.feedPet(slot.id, 2)
                                dialogueLine = "오도독 오도독! 고소한 견과류 꿀 쿠키라니 진짜 숲속 축제를 경험하는 맛이에요! 🍪✨"
                                scaleToggle = true
                            }
                        }
                    )

                    // Food Tier 3: Muffin
                    val canAffordT3 = mySparks >= 50
                    FoodItemRow(
                        title = "요정의 단풍벌집빵 🧁",
                        desc = "배부름 +100% | 성장률 폭증 보너스",
                        cost = "✨50",
                        isAffordable = canAffordT3,
                        onClick = {
                            if (slot.hungerPercent >= 0.99f) {
                                dialogueLine = "꺼어억~ 아주 기절할 것 같은 든든함이에요!"
                            } else if (!canAffordT3) {
                                dialogueLine = "정원의 이슬 Sparks가 모자라요... 숲속 탐험 소풍으로 수거할 수 있어요!"
                            } else {
                                viewModel.feedPet(slot.id, 3)
                                dialogueLine = "와아앙! 꿀맛 단풍 시럽이 가득 흘러요! 온몸이 따뜻해지고 보송 해졌어요! 🧁👑💖"
                                scaleToggle = true
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "정원으로 돌아가기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WarmBeige
    )
}

@Composable
fun FoodItemRow(
    title: String,
    desc: String,
    cost: String,
    isAffordable: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAffordable) { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAffordable) BoxBeige else StarSilver.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, if (isAffordable) LineBeige else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAffordable) DarkSlate else Color.Gray
                )
                Text(
                    text = desc,
                    fontSize = 8.sp,
                    color = if (isAffordable) DeepMocha.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier
                    .background(if (isAffordable) PeachAmber else Color.LightGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = cost,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
