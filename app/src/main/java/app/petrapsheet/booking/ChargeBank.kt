package app.petrapsheet.booking

import kotlin.random.Random

object ChargeBank {

    val dogCharges: List<Charge> = listOf(
        Charge("§ 14.7(a) Domestic Code", "Grand Theft Sock"),
        Charge("§ 14.7(b) Domestic Code", "Unlawful Digging in Fresh Flowerbed"),
        Charge("§ 22.3 Canine Code", "Failure to Disperse Post-Walk Mud"),
        Charge("§ 22.4 Canine Code", "Aggressive Tail Wagging Near Fragile Decor"),
        Charge("§ 31.1 Barking Regulations", "Public Disturbance via Postal Carrier Alert"),
        Charge("§ 31.2 Barking Regulations", "Excessive Vocalization at Inaudible Frequencies"),
        Charge("§ 45.6 Canine Code", "Counter Surfing in the First Degree"),
        Charge("§ 45.7 Canine Code", "Unauthorized Licking of Clean Dishwasher"),
        Charge("§ 52.8 Trash Ordinances", "Breaking and Entering Kitchen Wastebin"),
        Charge("§ 58.2 Leash Code", "Willful Disregard of Recall Command"),
        Charge("§ 60.1 Domestic Code", "Possession of Stolen Slipper with Intent to Chew"),
        Charge("§ 64.9 Bedding Act", "Refusal to Relinquish Center of King-Size Mattress"),
        Charge("§ 71.3 Table Manners Act", "Unlicensed Begging with Sorrowful Facial Expressions"),
        Charge("§ 73.1 Yard Protocol", "Unauthorized Excavation of Garden Fence Perimeter"),
        Charge("§ 77.4 Canine Code", "Pretending Food Bowl Has Never Once Been Filled"),
        Charge("§ 82.2 Domestic Code", "Public Zoomies with Severe Inability to Brake"),
        Charge("§ 85.5 Vehicle Code", "Head Protruding Unlawfully from Automobile Window"),
        Charge("§ 89.1 Puddle Regulations", "Intentional Immersion in Stagnant Mud Puddle"),
        Charge("§ 91.4 Canine Code", "Excessive Sniffing of Visiting Dignitaries"),
        Charge("§ 93.7 Property Code", "Deposition of Drool on Clean Trousers"),
    )

    val catCharges: List<Charge> = listOf(
        Charge("§ 12.1 Feline Ordinances", "Gravity Testing of Glassware from High Shelf"),
        Charge("§ 12.2 Feline Ordinances", "Loitering on the Active Computer Keyboard"),
        Charge("§ 18.4 Nighttime Protocol", "Conspiracy to Commit Zoomies at 3 a.m."),
        Charge("§ 18.5 Nighttime Protocol", "Unauthorized Operatic Vocal Solos at Dawn"),
        Charge("§ 27.3 Doorway Code", "Standing Indecisively in Open Threshold"),
        Charge("§ 33.6 Feline Ordinances", "Unlawful Occupancy of Fresh Cardboard Delivery Box"),
        Charge("§ 33.7 Feline Ordinances", "Desecration of Back of Living Room Armchair"),
        Charge("§ 41.2 Domestic Code", "Demanding Second Breakfast Under False Pretences"),
        Charge("§ 41.3 Domestic Code", "Refusing Premium Gourmet Pate After Single Sniff"),
        Charge("§ 48.9 Feline Ordinances", "Disrespectful Swatting of Innocent Canine Passersby"),
        Charge("§ 55.1 Personal Space Act", "Sudden and Unprovoked Lap Departure"),
        Charge("§ 55.2 Personal Space Act", "Sitting Directly on Suspect's Face at 6 a.m."),
        Charge("§ 62.4 Carpet Code", "Strategic Deposition of Hairball on Rare Wool Rug"),
        Charge("§ 67.8 Feline Ordinances", "Tripping Household Guardian on Staircase"),
        Charge("§ 70.1 Bedding Act", "Claiming Fresh Warm Laundry as Sovereign Territory"),
        Charge("§ 74.3 Water Ordinances", "Drinking from Bathroom Tap Instead of Ceramic Bowl"),
        Charge("§ 79.5 Surveillance Act", "Unblinking Stare at Completely Empty Living Room Wall"),
        Charge("§ 83.2 Feline Ordinances", "Loud Rhythmic Purring During Important Phone Calls"),
        Charge("§ 87.6 Domestic Code", "Willful Obstruction of Computer Display"),
        Charge("§ 94.1 Property Code", "Batting Stationery Under Heavy Appliance"),
    )

    val otherCharges: List<Charge> = listOf(
        Charge("§ 16.1 Habitat Ordinances", "Unauthorized Nibbling of Wooden Baseboards"),
        Charge("§ 16.2 Habitat Ordinances", "Disassembly of Insulated Appliance Wiring Sheath"),
        Charge("§ 24.3 Rodent Code", "Hoarding Seed Supplies in Secret Secondary Stash"),
        Charge("§ 24.4 Rodent Code", "Midnight Exercise Wheel Squeaking Without Permit"),
        Charge("§ 29.5 Avian Ordinances", "Imitating Microwave Beeps to Cause Household Alarm"),
        Charge("§ 29.6 Avian Ordinances", "Aerial Distribution of Seed Husks Beyond Cage Perimeter"),
        Charge("§ 35.2 Reptile Protocol", "Unauthorized Basking on Warm Network Router"),
        Charge("§ 35.3 Reptile Protocol", "Refusing to Disclose Hiding Location Behind Bookshelf"),
        Charge("§ 43.1 Aquatic Code", "Splashing Water Over Top Edge of Aquarium"),
        Charge("§ 43.2 Aquatic Code", "Feigning Extreme Hunger Despite Recent Feeding"),
        Charge("§ 50.4 Habitat Ordinances", "Excavation into Interior Sofa Foam Padding"),
        Charge("§ 50.5 Habitat Ordinances", "Shredding Important Paperwork for Nesting Material"),
        Charge("§ 57.2 Avian Ordinances", "Vigorous Screaming in Celebration of Ordinary Sunlight"),
        Charge("§ 57.3 Avian Ordinances", "Nipping Friendly Finger of Household Caregiver"),
        Charge("§ 65.7 Enclosure Code", "Escaping Terrarium During Arrival of Dinner Guests"),
        Charge("§ 68.3 Rodent Code", "Excessive Dental Chattering Creating General Concern"),
        Charge("§ 72.8 Habitat Ordinances", "Uprooting Prized Indoor Fern Roots"),
        Charge("§ 76.4 Pocket Pet Code", "Concealing Baby Carrots Underneath Living Room Rug"),
        Charge("§ 81.9 Avian Ordinances", "Mimicking Doorbell Chime During Designated Quiet Hours"),
        Charge("§ 88.5 Enclosure Code", "Deliberate Overturning of Ceramic Water Dish"),
    )

    val agnosticCharges: List<Charge> = listOf(
        Charge("§ 10.1 Domestic Code", "Failure to Disperse from High-Traffic Furniture"),
        Charge("§ 10.2 Domestic Code", "Unlawful Shedding of Fur on Black Formal Attire"),
        Charge("§ 15.3 Household Regulations", "Selective Hearing During Urgent Verbal Commands"),
        Charge("§ 15.4 Household Regulations", "Auditory Sensitivity Only to Crinkling Treat Packaging"),
        Charge("§ 20.5 Domestic Code", "Unauthorized Ingestion of Non-Toxic Houseplant Foliage"),
        Charge("§ 25.1 Napping Ordinances", "Sleeping Horizontally Across Main Household Hallway"),
        Charge("§ 25.2 Napping Ordinances", "Excessive Snoring During Climax of Television Program"),
        Charge("§ 38.3 Domestic Code", "Theft of Warm Seat Cushion Immediately Upon Departure"),
        Charge("§ 38.4 Domestic Code", "Obstruction of Narrow Kitchen Pathway While Cooking"),
        Charge("§ 47.2 Household Regulations", "Evading Scheduled Veterinary Departure Under Sofa"),
        Charge("§ 47.3 Household Regulations", "Theatrical Protests During Routine Claw Grooming"),
        Charge("§ 53.6 Domestic Code", "Unlicensed Panhandling at the Formal Dining Table"),
        Charge("§ 59.1 Snack Protocol", "Demanding Biscuits Without Executing Requested Trick"),
        Charge("§ 63.5 Household Regulations", "Silently Judging Household Members for Life Choices"),
        Charge("§ 69.2 Domestic Code", "Unauthorized Possession and Transport of Dirty Sock"),
        Charge("§ 75.8 Schedule Code", "Refusal to Acknowledge Official Household Bedtime"),
        Charge("§ 80.4 Domestic Code", "Hostile Standoff with Operating Vacuum Cleaner"),
        Charge("§ 84.7 Household Regulations", "Misrepresenting Nutritional Status to House Guests"),
        Charge("§ 92.3 Domestic Code", "Failure to Yield Right of Way to Human Footsteps"),
        Charge("§ 96.1 Property Code", "Leaving Trail of Shredded Cardboard Through Foyer"),
    )

    val allCharges: List<Charge> = dogCharges + catCharges + otherCharges + agnosticCharges

    fun chargesFor(species: Species): List<Charge> = when (species) {
        Species.DOG -> dogCharges + agnosticCharges
        Species.CAT -> catCharges + agnosticCharges
        Species.OTHER -> otherCharges + agnosticCharges
    }

    /**
     * Deterministic, non-repeating selection of [count] charges for a given [species] and [seed].
     */
    fun randomCharges(species: Species, seed: Long, count: Int = 3): List<Charge> {
        val pool = chargesFor(species)
        val k = count.coerceIn(2, minOf(4, pool.size))
        val random = Random(seed)
        return pool.shuffled(random).take(k)
    }

    /** Default fallback charges for initial display. */
    fun defaultCharges(species: Species): List<Charge> = chargesFor(species).take(3)

    /** Generates a deadpan statute for a custom user-typed charge. */
    fun customStatute(index: Int = 1): String = "§ 99.$index Domestic Discretionary Code"
}
