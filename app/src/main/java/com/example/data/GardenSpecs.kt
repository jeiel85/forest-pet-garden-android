package com.example.data

enum class Rarity(val label: String) {
    COMMON("일반"),
    UNCOMMON("고급"),
    RARE("희귀"),
    EPIC("영웅"),
    LEGENDARY("전설")
}

data class PetSpec(
    val id: Int,
    val name: String,
    val breed: String,
    val emoji: String,
    val lore: String,
    val rarity: Rarity,
    val adoptCost: Int,
    val preferredFood: String,
    val preferredToy: String
)

data class AdventureAreaSpec(
    val id: Int,
    val name: String,
    val emoji: String,
    val requiredLevel: Int, // Garden level required
    val explorationSeconds: Int,
    val desc: String
)

data class CollectionItemSpec(
    val id: Int,
    val name: String,
    val emoji: String,
    val rarity: Rarity,
    val lore: String,
    val sourceAreaName: String
)

object GardenSpecs {
    val pets = listOf(
        PetSpec(
            id = 1,
            name = "아라",
            breed = "아기 여우",
            emoji = "🦊",
            lore = "은은한 노을빛을 닮은 털을 가진 장난꾸러기입니다. 기쁠 땐 꼬리를 살랑이며 깡충 뛰어오르는 버릇이 있어요.",
            rarity = Rarity.COMMON,
            adoptCost = 10,
            preferredFood = "달콤 과일 사과🍎",
            preferredToy = "대롱 깃털 놀이🪶"
        ),
        PetSpec(
            id = 2,
            name = "두두",
            breed = "잠꾸러기 코알라",
            emoji = "🐨",
            lore = "온순하고 잠이 많아 늘 나뭇가지나 포근한 구름베개에 기대어 있습니다. 안아주는 것을 제일 좋아합니다.",
            rarity = Rarity.COMMON,
            adoptCost = 50,
            preferredFood = "바스락 유칼립 주스🧉",
            preferredToy = "말랑 나뭇가지 정돈🪵"
        ),
        PetSpec(
            id = 3,
            name = "샤샤",
            breed = "발랄한 아기 토끼",
            emoji = "🐰",
            lore = "하프 소리와 산바람 소리를 반기며 귀를 쫑긋 세우고, 숲속 소풍을 가장 즐기는 활발한 토끼 요정입니다.",
            rarity = Rarity.UNCOMMON,
            adoptCost = 150,
            preferredFood = "꼬소한 숲속 당근🥕",
            preferredToy = "통통 굴러가는 털실뭉치🧶"
        ),
        PetSpec(
            id = 4,
            name = "벼리",
            breed = "햇살 다람쥐",
            emoji = "🐿️",
            lore = "도토리를 소중하게 모으는 데 도가 튼 다람쥐입니다. 볼 가득 먹이를 넣고 눈을 끔벅이며 귀여움을 부립니다.",
            rarity = Rarity.UNCOMMON,
            adoptCost = 400,
            preferredFood = "우두둑 달콤 견과류🌰",
            preferredToy = "굴러가는 보송 방울 발자국🎾"
        ),
        PetSpec(
            id = 5,
            name = "삐약이",
            breed = "따스한 아기 오리",
            emoji = "🐥",
            lore = "맑고 소담스러운 조약돌 물웅덩이를 찾아 아장아장 걷습니다. 조잘거리며 아침 이슬 노래를 하곤 해요.",
            rarity = Rarity.RARE,
            adoptCost = 1200,
            preferredFood = "보송 개구리밥 비스킷🥖",
            preferredToy = "퐁당 대나무 비눗방울⛲"
        ),
        PetSpec(
            id = 6,
            name = "뭉치",
            breed = "동글곰",
            emoji = "🐻",
            lore = "따뜻하고 보송한 숲속 산딸기를 좋아하며 꿀 한 티스푼을 주면 온 몸을 둥글게 흔들며 미소 짓는 순한 곰입니다.",
            rarity = Rarity.RARE,
            adoptCost = 3500,
            preferredFood = "꿀이 뚝뚝 흐르는 벌집🍯",
            preferredToy = "따스한 퐁퐁 등불🏮"
        ),
        PetSpec(
            id = 7,
            name = "은하별",
            breed = "별빛 요정 벼리캣",
            emoji = "🐱",
            lore = "눈안에 은하수 빛깔 무늬가 부드럽게 흩뿌려져 있는 조용하고 신비로운 숲속 우주 고양이입니다.",
            rarity = Rarity.EPIC,
            adoptCost = 10000,
            preferredFood = "반짝 은빛 은하 비스킷⭐",
            preferredToy = "반짝 별무리 렌즈안경🔮"
        ),
        PetSpec(
            id = 8,
            name = "현자 부우",
            breed = "학자 밤부엉이",
            emoji = "🦉",
            lore = "보름달이 뜨는 밤에만 온화하게 눈을 뜨는 학식 높은 밤부엉이 요정입니다. 숲속 비밀 책을 안고 숨바꼭질합니다.",
            rarity = Rarity.LEGENDARY,
            adoptCost = 25000,
            preferredFood = "달콤 청포도 젤리🍇",
            preferredToy = "수수께끼 오래된 양장책📖"
        )
    )

    val areas = listOf(
        AdventureAreaSpec(
            id = 1,
            name = "소담스런 수풀 (Whispering Thicket)",
            emoji = "🌿",
            requiredLevel = 1,
            explorationSeconds = 20, // Quick test
            desc = "낮은 나뭇잎 사이로 햇살이 스며들어 풀벌레들이 연주를 마친 온화한 연록빛 수풀입니다."
        ),
        AdventureAreaSpec(
            id = 2,
            name = "비밀의 비밀길 (Secret Glade)",
            emoji = "🏡",
            requiredLevel = 2,
            explorationSeconds = 60, // 1 min
            desc = "안개너머 숨겨져 우체통만이 반갑게 기웃대는, 요정들이 차 한 모금 마시러 찾아온다는 비밀 공터."
        ),
        AdventureAreaSpec(
            id = 3,
            name = "은빛 비갠 선큰못 (Sunken Pond)",
            emoji = "⛲",
            requiredLevel = 3,
            explorationSeconds = 180, // 3 mins
            desc = "비갠 하늘 대나무 물줄기 소리가 잔잔히 퍼지는 보랏빛 산란광 아래 고즈넉이 정돈된 연못가."
        ),
        AdventureAreaSpec(
            id = 4,
            name = "은빛 풍경 별무리 고개 (Starlight Peak)",
            emoji = "⭐",
            requiredLevel = 4,
            explorationSeconds = 300, // 5 mins
            desc = "귓가에 숲속 맑은 풍경 종 소리와 함께 우주에서 은가루 별빛이 가슴 아늑히 소용돌이치는 꼭대기 언덕."
        ),
        AdventureAreaSpec(
            id = 5,
            name = "고대 고목 등불아래 (Ancient Hollow)",
            emoji = "🌳",
            requiredLevel = 5,
            explorationSeconds = 600, // 10 mins
            desc = "정원의 수천년 고목이 품어주는 거대한 나무 안쪽 동굴로, 고요한 평온과 귀중한 은빛 유물들이 잠든 곳."
        )
    )

    val collectionItems = listOf(
        CollectionItemSpec(id = 1, name = "반짝 별빛 조약돌", emoji = "⭐", rarity = Rarity.COMMON, lore = "밤이 밀려와도 작은 노란 윤슬을 내뿜는, 손바닥에 꼭 쥐면 온기가 생기는 조약돌.", sourceAreaName = "소담스런 수풀"),
        CollectionItemSpec(id = 2, name = "황금 오크 잎새", emoji = "🍁", rarity = Rarity.COMMON, lore = "언제나 바삭한 아침 소리를 내며 영원히 주황빛 단풍을 머금은 마법의 넙적한 가을 잎새.", sourceAreaName = "비밀의 비밀길"),
        CollectionItemSpec(id = 3, name = "이끼 낀 고대 청동 열쇠", emoji = "🔑", rarity = Rarity.UNCOMMON, lore = "에메랄드 이끼가 살포시 둘러싸고 있는, 어디 조그마한 오르골 상자를 열 수 있을 것만 같은 청동 열쇠.", sourceAreaName = "은빛 비갠 선큰못"),
        CollectionItemSpec(id = 4, name = "무지개 이슬 깃털", emoji = "🪶", rarity = Rarity.UNCOMMON, lore = "바람에 날려온 뒤 무지개 이슬을 머금고 햇살에 비치면 오색 찬란 성스러운 수채화 무늬를 내 비추는 깃털.", sourceAreaName = "은빛 풍경 별무리 고개"),
        CollectionItemSpec(id = 5, name = "클로버 바람 나침반", emoji = "🧭", rarity = Rarity.UNCOMMON, lore = "가리키는 바늘이 언제나 정원의 기적과 아기 오리들의 소풍길을 지름길로 안내해주는 따뜻한 나무 나침반.", sourceAreaName = "소담스런 수풀"),
        CollectionItemSpec(id = 6, name = "달기만 한 단풍 메이플 시럽", emoji = "🥞", rarity = Rarity.RARE, lore = "고목 밑에서 요정들이 가마솥에 은은히 달여 만든, 뚜껑을 열기만 해도 숲 전체가 달달해지는 기적의 영양 단풍 시럽.", sourceAreaName = "고대 고목 등불아래"),
        CollectionItemSpec(id = 7, name = "소원을 품은 촉촉 산호", emoji = "🪸", rarity = Rarity.RARE, lore = "은빛 연못 물고기들이 기도가 모여 신기하게 수면 밖으로 돋아난, 옅은 핑크빛의 소중한 해초 산호송이.", sourceAreaName = "은빛 비갠 선큰못"),
        CollectionItemSpec(id = 8, name = "요정의 별가루 유리병", emoji = "🧪", rarity = Rarity.RARE, lore = "별들이 자러 갈 때 소량으로 흘리고 가 흔들어보면 찰랑이는 밤하늘이 담겨있는 극도로 은밀한 요정 병.", sourceAreaName = "은빛 풍경 별무리 고개"),
        CollectionItemSpec(id = 9, name = "잊혀진 엘프 자개화", emoji = "🏵️", rarity = Rarity.EPIC, lore = "찬란한 금실로 짜인 유적 속 동전 위에 신화 속 꽃 모양 무늬가 아름답게 정박해 있는 환상 유산.", sourceAreaName = "고대 고목 등불아래"),
        CollectionItemSpec(id = 10, name = "꿈꾸는 보랏빛 야광 버섯", emoji = "🍄", rarity = Rarity.EPIC, lore = "수면 아래 조용하게 어루만지는 보라색 안개를 뿜으며, 피곤한 동물의 마음에 평온을 깔아주는 신비한 식재료 야광 버섯.", sourceAreaName = "소담스런 수풀"),
        CollectionItemSpec(id = 11, name = "숲바람 수호 은도금 바람개비", emoji = "🎐", rarity = Rarity.LEGENDARY, lore = "미소지으며 바람개비를 불어주면 주변 나쁜 구름을 단번에 흩어 평화로 가득 찬 쾌적한 숲길을 열어 주는 전설의 보물.", sourceAreaName = "은빛 비갠 선큰못"),
        CollectionItemSpec(id = 12, name = "달 그림자 솔방울 펜던트", emoji = "🏺", rarity = Rarity.LEGENDARY, lore = "시간의 수호 숲 보물이 세공해 둔 영험한 호두 모양의 이슬 결정 악세서리로, 착용한 자의 운명을 환히 조명하며 숲의 이슬을 마르지 않게 돕는 신화 결정체.", sourceAreaName = "고대 고목 등불아래")
    )

    fun getPetById(id: Int): PetSpec? = pets.find { it.id == id }
    fun getAreaById(id: Int): AdventureAreaSpec? = areas.find { it.id == id }
    fun getCollectionItemById(id: Int): CollectionItemSpec? = collectionItems.find { it.id == id }
}
