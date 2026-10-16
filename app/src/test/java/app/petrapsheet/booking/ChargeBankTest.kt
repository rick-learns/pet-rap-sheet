package app.petrapsheet.booking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargeBankTest {
    private val statuteRegex = Regex("""^§\s+\d+(\.\d+)?(\([a-z]\))?\s+.+$""")

    @Test
    fun totalChargesCountIsAtLeast60() {
        assertTrue(
            "Expected at least 60 total charges, was ${ChargeBank.allCharges.size}",
            ChargeBank.allCharges.size >= 60,
        )
    }

    @Test
    fun dogChargesCountIsAtLeast15() {
        assertTrue(
            "Expected at least 15 dog charges, was ${ChargeBank.dogCharges.size}",
            ChargeBank.dogCharges.size >= 15,
        )
    }

    @Test
    fun catChargesCountIsAtLeast15() {
        assertTrue(
            "Expected at least 15 cat charges, was ${ChargeBank.catCharges.size}",
            ChargeBank.catCharges.size >= 15,
        )
    }

    @Test
    fun otherChargesCountIsAtLeast15() {
        assertTrue(
            "Expected at least 15 other charges, was ${ChargeBank.otherCharges.size}",
            ChargeBank.otherCharges.size >= 15,
        )
    }

    @Test
    fun speciesAgnosticPoolIsNotEmpty() {
        assertTrue(
            "Expected agnostic charges, was ${ChargeBank.agnosticCharges.size}",
            ChargeBank.agnosticCharges.size >= 15,
        )
    }

    @Test
    fun totalCountEqualsSumOfDistinctPools() {
        val totalExpected = ChargeBank.dogCharges.size +
            ChargeBank.catCharges.size +
            ChargeBank.otherCharges.size +
            ChargeBank.agnosticCharges.size
        assertEquals(totalExpected, ChargeBank.allCharges.size)
    }

    @Test
    fun noChargeDescriptionRepeats() {
        val descriptions = ChargeBank.allCharges.map { it.description.trim().lowercase() }
        val duplicates = descriptions.groupingBy { it }.eachCount().filter { it.value > 1 }
        assertTrue("Duplicate charge descriptions found: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun noStatuteRepeats() {
        val statutes = ChargeBank.allCharges.map { it.statute.trim() }
        val duplicates = statutes.groupingBy { it }.eachCount().filter { it.value > 1 }
        assertTrue("Duplicate statutes found: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun everyStatuteMatchesDeadpanFormat() {
        for (charge in ChargeBank.allCharges) {
            assertTrue(
                "Statute '${charge.statute}' for '${charge.description}' does not match expected deadpan format",
                statuteRegex.matches(charge.statute),
            )
        }
    }

    @Test
    fun randomChargesAreDeterministicForSeedAndSpecies() {
        for (species in Species.entries) {
            for (seed in listOf(0L, 42L, 12345L, -99L, Long.MAX_VALUE)) {
                val first = ChargeBank.randomCharges(species, seed)
                val second = ChargeBank.randomCharges(species, seed)
                assertEquals("Random picks must be deterministic for seed $seed and species $species", first, second)
            }
        }
    }

    @Test
    fun randomChargesNeverRepeatWithinSingleSelection() {
        for (species in Species.entries) {
            for (seed in 0L until 100L) {
                val charges = ChargeBank.randomCharges(species, seed, count = 4)
                assertEquals(
                    "Selection for seed $seed contained duplicate charges",
                    charges.size,
                    charges.distinctBy { it.description }.size,
                )
            }
        }
    }

    @Test
    fun randomChargesPicksWithinRequestedBounds() {
        for (count in 2..4) {
            val charges = ChargeBank.randomCharges(Species.DOG, 42L, count = count)
            assertEquals(count, charges.size)
        }
    }

    @Test
    fun differentSeedsYieldDifferentSelections() {
        val first = ChargeBank.randomCharges(Species.DOG, 1L)
        val second = ChargeBank.randomCharges(Species.DOG, 2L)
        assertNotEquals(first, second)
    }
}
