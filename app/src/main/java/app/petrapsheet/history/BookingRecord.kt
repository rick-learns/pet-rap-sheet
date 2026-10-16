package app.petrapsheet.history

import app.petrapsheet.booking.Booking
import app.petrapsheet.booking.Charge
import app.petrapsheet.booking.Species
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * A persisted booking record containing all inputs needed to regenerate identical documents.
 */
data class BookingRecord(
    val id: String,
    val name: String,
    val species: Species,
    val alias: String?,
    val town: String?,
    val officer: String?,
    val charges: List<Charge>,
    val seed: Long,
    val bookedOnEpochDay: Long,
    val precinctCamera: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val bookedOn: LocalDate
        get() = LocalDate.ofEpochDay(bookedOnEpochDay)

    fun toBooking(): Booking = Booking.create(
        name = name,
        species = species,
        alias = alias,
        town = town,
        seed = seed,
        bookedOn = bookedOn,
        charges = charges,
        arrestingOfficer = officer,
    )

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("species", species.name)
        if (alias != null) obj.put("alias", alias)
        if (town != null) obj.put("town", town)
        if (officer != null) obj.put("officer", officer)
        val chargesArray = JSONArray()
        for (charge in charges) {
            val c = JSONObject()
            c.put("statute", charge.statute)
            c.put("description", charge.description)
            chargesArray.put(c)
        }
        obj.put("charges", chargesArray)
        obj.put("seed", seed)
        obj.put("bookedOnEpochDay", bookedOnEpochDay)
        obj.put("precinctCamera", precinctCamera)
        obj.put("createdAt", createdAt)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): BookingRecord {
            val obj = JSONObject(jsonStr)
            val chargesList = mutableListOf<Charge>()
            val chargesArray = obj.optJSONArray("charges")
            if (chargesArray != null) {
                for (i in 0 until chargesArray.length()) {
                    val c = chargesArray.getJSONObject(i)
                    chargesList.add(Charge(c.getString("statute"), c.getString("description")))
                }
            }
            return BookingRecord(
                id = obj.getString("id"),
                name = obj.getString("name"),
                species = Species.valueOf(obj.getString("species")),
                alias = obj.optString("alias").takeIf { it.isNotEmpty() },
                town = obj.optString("town").takeIf { it.isNotEmpty() },
                officer = obj.optString("officer").takeIf { it.isNotEmpty() },
                charges = chargesList,
                seed = obj.getLong("seed"),
                bookedOnEpochDay = obj.getLong("bookedOnEpochDay"),
                precinctCamera = obj.optBoolean("precinctCamera", false),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            )
        }
    }
}
