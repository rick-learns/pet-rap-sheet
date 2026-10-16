package app.petrapsheet.booking

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ensures charges describe only harmless, playful pet shenanigans and never touch real crimes,
 * cruelty, weapons, drugs, slurs or abuse.
 */
class ChargeBankContentTest {
    private val denyList = listOf(
        // Weapons
        "gun", "firearm", "pistol", "rifle", "shotgun", "knife", "blade", "sword", "dagger",
        "weapon", "bomb", "explosive", "grenade", "shoot", "stab",
        // Drugs
        "drug", "narcotic", "cocaine", "heroin", "meth", "marijuana", "weed", "fentanyl",
        "opioid", "pill", "overdose", "intoxica",
        // Cruelty / Abuse / Violence against animals or humans
        "abuse", "cruel", "torture", "beat", "hit", "strike", "wound", "injure", "bleed",
        "blood", "strangle", "choke", "poison", "starve", "neglect",
        // Real violent crimes against people
        "murder", "homicide", "manslaughter", "assault", "battery", "kidnap", "abduct",
        "hostage", "arson", "rape", "extort", "terror",
    )

    private fun matchesDenyList(text: String): List<String> {
        val lower = text.lowercase()
        return denyList.filter { word ->
            Regex("""\b${Regex.escape(word)}[a-z]*\b""").containsMatchIn(lower)
        }
    }

    @Test
    fun noChargeInBankMatchesDenyList() {
        for (charge in ChargeBank.allCharges) {
            val statuteMatches = matchesDenyList(charge.statute)
            val descMatches = matchesDenyList(charge.description)
            assertTrue(
                "Statute '${charge.statute}' matches deny-list terms: $statuteMatches",
                statuteMatches.isEmpty(),
            )
            assertTrue(
                "Charge '${charge.description}' matches deny-list terms: $descMatches",
                descMatches.isEmpty(),
            )
        }
    }

    @Test
    fun validatorCatchesForbiddenWords() {
        assertFalse(matchesDenyList("Carrying an unlicensed firearm").isEmpty())
        assertFalse(matchesDenyList("Cruel treatment of sofa").isEmpty())
        assertFalse(matchesDenyList("Possession of narcotic treats").isEmpty())
        assertFalse(matchesDenyList("First degree murder of a mouse").isEmpty())
    }
}
