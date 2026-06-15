package com.jeiel85.forestpetgarden

import com.jeiel85.forestpetgarden.data.GardenSpecs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the static game catalog (pets, adventure areas, collectibles) is internally
 * consistent. Pure JVM test — no Android framework required.
 */
class GardenSpecsTest {

    @Test
    fun catalog_hasExpectedCounts() {
        assertEquals(8, GardenSpecs.pets.size)
        assertEquals(5, GardenSpecs.areas.size)
        assertEquals(12, GardenSpecs.collectionItems.size)
    }

    @Test
    fun petIds_areUniqueAndSequential() {
        val ids = GardenSpecs.pets.map { it.id }
        assertEquals(ids.toSet().size, ids.size)
        assertEquals((1..GardenSpecs.pets.size).toList(), ids.sorted())
    }

    @Test
    fun adoptCost_increasesWithRarity() {
        val costs = GardenSpecs.pets.map { it.adoptCost }
        assertEquals(costs.sorted(), costs)
        assertTrue(costs.all { it > 0 })
    }

    @Test
    fun lookupHelpers_resolveAndMiss() {
        assertNotNull(GardenSpecs.getPetById(1))
        assertNotNull(GardenSpecs.getAreaById(1))
        assertNotNull(GardenSpecs.getCollectionItemById(1))
        assertNull(GardenSpecs.getPetById(999))
        assertEquals("아라", GardenSpecs.getPetById(1)?.name)
    }

    @Test
    fun everyCollectible_referencesAKnownArea() {
        val areaNames = GardenSpecs.areas.map { it.name }
        GardenSpecs.collectionItems.forEach { item ->
            assertTrue(
                "Collectible '${item.name}' points at unknown area '${item.sourceAreaName}'",
                areaNames.any { it.contains(item.sourceAreaName) }
            )
        }
    }
}
