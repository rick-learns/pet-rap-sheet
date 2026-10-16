package app.petrapsheet.booking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BookingTest {
    private val pattern = Regex("""^2026-\d{5}$""")

    @Test
    fun bookingNumberIsDeterministicForASeed() {
        for (seed in listOf(0L, 1L, 42L, -7L, Long.MAX_VALUE, Long.MIN_VALUE)) {
            assertEquals(BookingNumbers.fromSeed(seed), BookingNumbers.fromSeed(seed))
        }
    }

    @Test
    fun bookingNumberAlwaysMatchesTheFormat() {
        val seeds = (-2_000L..2_000L) + listOf(Long.MAX_VALUE, Long.MIN_VALUE)
        for (seed in seeds) {
            val number = BookingNumbers.fromSeed(seed)
            assertTrue("seed $seed gave $number", pattern.matches(number))
        }
    }

    @Test
    fun differentSeedsGiveDifferentNumbers() {
        val numbers = (0L until 500L).map(BookingNumbers::fromSeed).toSet()
        assertTrue("only ${numbers.size} distinct numbers from 500 seeds", numbers.size > 480)
        assertNotEquals(BookingNumbers.fromSeed(1), BookingNumbers.fromSeed(2))
    }

    @Test
    fun noTownMeansCountyOfHousehold() {
        for (town in listOf(null, "", "   ")) {
            val booking = booking(town)
            assertEquals("HOUSEHOLD", booking.county)
            assertEquals("COUNTY OF HOUSEHOLD · BOOKING NO. ${booking.bookingNumber}", booking.placardLine)
        }
    }

    @Test
    fun townIsUpperCasedOnThePlacard() {
        val booking = booking(" Little Snoring ")
        assertEquals("Little Snoring", booking.town)
        assertEquals("COUNTY OF LITTLE SNORING · BOOKING NO. ${booking.bookingNumber}", booking.placardLine)
    }

    @Test
    fun blankOptionalFieldsAreDropped() {
        val booking = Booking.create("  Mittens ", Species.CAT, "  ", "", seed = 3, bookedOn = LocalDate.of(2026, 1, 2))
        assertEquals("Mittens", booking.name)
        assertNull(booking.alias)
        assertNull(booking.town)
        assertEquals("02 JAN 2026", booking.bookedOnText)
    }

    @Test
    fun defaultOfficerIsOfficerOnDuty() {
        val booking = booking(null)
        assertEquals("Officer On Duty", booking.officer)
    }

    @Test
    fun customOfficerIsTrimmedAndUsed() {
        val booking = Booking.create(
            name = "Biscuit",
            species = Species.DOG,
            alias = null,
            town = null,
            seed = 42L,
            bookedOn = LocalDate.of(2026, 10, 16),
            arrestingOfficer = "  Sergeant Barkowitz  ",
        )
        assertEquals("Sergeant Barkowitz", booking.officer)
    }

    @Test
    fun judgesListHasAtLeast12Judges() {
        assertTrue("Judges list must have at least 12 entries", Judges.ALL.size >= 12)
        for (judge in Judges.ALL) {
            assertTrue("Judge name '$judge' must start with Honorable Judge", judge.startsWith("Honorable Judge"))
        }
    }

    @Test
    fun hearingDateIsBetween7And30DaysAfterBooking() {
        val bookedOn = LocalDate.of(2026, 10, 16)
        for (seed in -500L..500L) {
            val booking = Booking.create("Suspect", Species.DOG, null, null, seed, bookedOn)
            val daysAfter = java.time.temporal.ChronoUnit.DAYS.between(booking.bookedOn, booking.hearingDate)
            assertTrue("Hearing date was $daysAfter days after booking for seed $seed", daysAfter in 7..30)
        }
    }

    @Test
    fun bailIsSetInTreats() {
        val booking = booking(null)
        assertTrue("Bail must be non-empty", booking.bail.isNotBlank())
        assertTrue("Bail should mention treats or snacks", booking.bail.split(" ").size >= 2)
    }

    @Test
    fun explicitChargesArePreserved() {
        val charges = listOf(
            Charge("§ 1.1", "Charge A"),
            Charge("§ 2.2", "Charge B"),
        )
        val booking = Booking.create(
            name = "Biscuit",
            species = Species.DOG,
            alias = null,
            town = null,
            seed = 42L,
            bookedOn = LocalDate.of(2026, 10, 16),
            charges = charges,
        )
        assertEquals(charges, booking.charges)
    }

    private fun booking(town: String?) =

        Booking.create("Biscuit", Species.DOG, "Sir Floofington", town, seed = 42, bookedOn = LocalDate.of(2026, 10, 16))
}
