package app.petrapsheet.booking

/** One deadpan charge against a suspect, with its domestic statute number. */
data class Charge(
    val statute: String,
    val description: String,
)
