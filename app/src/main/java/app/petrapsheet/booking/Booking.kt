package app.petrapsheet.booking

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

enum class Species { DOG, CAT, OTHER }

/** One booking: everything a document needs to know about the suspect. */
data class Booking(
    val name: String,
    val species: Species,
    val alias: String?,
    val town: String?,
    val bookingNumber: String,
    val bookedOn: LocalDate,
    val charges: List<Charge>,
    val arrestingOfficer: String? = null,
    val bail: String,
    val hearingDate: LocalDate,
    val judge: String,
) {
    /** The county on the placard: the town, upper-cased, or HOUSEHOLD when none was given. */
    val county: String
        get() = town?.trim()?.takeIf { it.isNotEmpty() }?.uppercase(Locale.US) ?: DEFAULT_COUNTY

    val placardLine: String
        get() = "COUNTY OF $county · BOOKING NO. $bookingNumber"

    val bookedOnText: String
        get() = DATE_FORMAT.format(bookedOn).uppercase(Locale.US)

    val hearingDateText: String
        get() = DATE_FORMAT.format(hearingDate).uppercase(Locale.US)

    val officer: String
        get() = arrestingOfficer?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_OFFICER

    val caseNumber: String
        get() = bookingNumber

    companion object {
        const val DEFAULT_COUNTY = "HOUSEHOLD"
        const val DEFAULT_OFFICER = "Officer On Duty"
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

        /** Builds a booking from form input. Blank optional fields become null. */
        fun create(
            name: String,
            species: Species,
            alias: String?,
            town: String?,
            seed: Long,
            bookedOn: LocalDate,
            charges: List<Charge> = emptyList(),
            arrestingOfficer: String? = null,
        ): Booking {
            val resolvedCharges = if (charges.isNotEmpty()) charges else ChargeBank.randomCharges(species, seed, 3)
            val hearingDaysOffset = 7L + Random(seed + 101).nextInt(24) // 7 to 30 days
            val hearingDate = bookedOn.plusDays(hearingDaysOffset)
            val judge = Judges.fromSeed(seed)
            val bail = BailTreats.from(species, seed)

            return Booking(
                name = name.trim(),
                species = species,
                alias = alias?.trim()?.takeIf { it.isNotEmpty() },
                town = town?.trim()?.takeIf { it.isNotEmpty() },
                bookingNumber = BookingNumbers.fromSeed(seed),
                bookedOn = bookedOn,
                charges = resolvedCharges,
                arrestingOfficer = arrestingOfficer?.trim()?.takeIf { it.isNotEmpty() },
                bail = bail,
                hearingDate = hearingDate,
                judge = judge,
            )
        }
    }
}

object Judges {
    val ALL = listOf(
        "Honorable Judge Whiskers",
        "Honorable Judge Barkington",
        "Honorable Judge Pawsner",
        "Honorable Judge Fluffbottom",
        "Honorable Judge Clawford",
        "Honorable Judge Wigglebutt",
        "Honorable Judge Barnaby Snoot",
        "Honorable Judge Penelope Purr",
        "Honorable Judge Archibald Featherstone",
        "Honorable Judge Bartholomew Woof",
        "Honorable Judge Tabitha Snuggles",
        "Honorable Judge H. R. Paws",
        "Honorable Judge Maximilian Von Flea",
        "Honorable Judge Reginald Featherbottom",
        "Honorable Judge Beatrice Von Meow",
        "Honorable Judge Oliver Snout",
    )

    fun fromSeed(seed: Long): String {
        val index = (Random(seed + 202).nextInt(Int.MAX_VALUE) % ALL.size)
        return ALL[index]
    }
}

object BailTreats {
    private val AMOUNTS = listOf("100", "250", "500", "750", "1,000", "2,500", "5,000")

    private val DOG_TREATS = listOf(
        "BACON STRIPS",
        "CHEDDAR SQUARES",
        "CRUNCHY BISCUITS",
        "DENTAL CHEWS",
        "LIVER BITES",
        "PEANUT BUTTER CUPS",
    )

    private val CAT_TREATS = listOf(
        "SALMON CRUNCHIES",
        "TUNA FLAKES",
        "CATNIP DROPS",
        "POULTRY BITES",
        "CHEESE MORSELS",
        "CHICKEN SNACKS",
    )

    private val OTHER_TREATS = listOf(
        "YOGURT DROPS",
        "DRIED CRANBERRIES",
        "SEED CLUSTERS",
        "CARROT MEDALLIONS",
        "APPLE CRISPS",
        "ALFALFA PELLETS",
    )

    fun from(species: Species, seed: Long): String {
        val random = Random(seed + 303)
        val amount = AMOUNTS[random.nextInt(AMOUNTS.size)]
        val items = when (species) {
            Species.DOG -> DOG_TREATS
            Species.CAT -> CAT_TREATS
            Species.OTHER -> OTHER_TREATS
        }
        val item = items[random.nextInt(items.size)]
        return "$amount $item"
    }
}

object BookingNumbers {
    private const val YEAR = "2026"

    /** The same seed always gives the same number, shaped like 2026-04217. */
    fun fromSeed(seed: Long): String {
        val serial = Random(seed).nextInt(1, 100_000)
        return "$YEAR-${serial.toString().padStart(5, '0')}"
    }
}
