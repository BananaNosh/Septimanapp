package com.nobodysapps.septimanapp.viewModel

import android.content.Context
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.model.EatingHabit
import com.nobodysapps.septimanapp.model.EnrolInformation
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_YES
import com.nobodysapps.septimanapp.model.create
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_ENROLLED
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_IN_PROGRESS
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_NOT_ASK_AGAIN
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_REMIND
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.SeptimanaLocation
import com.nobodysapps.septimanapp.notifications.AlarmScheduler
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Locale

/**
 * Holds the enrolment form's business logic: field persistence, the enrol-state/reminder
 * machine, and the enrolment email assembly. The fragment keeps only view wiring.
 */
class EnrolmentViewModel(
    private val informationStorage: EnrolInformationStorage,
    private val eventInfoStorage: EventInfoStorage,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val septimanaLocation: SeptimanaLocation
        get() = eventInfoStorage.loadSeptimanaLocation()

    fun loadEnrolInformation(): EnrolInformation = informationStorage.loadEnrolInformation()

    fun countryList(): List<String> =
        Locale.getAvailableLocales()
            .map { it.displayCountry }
            .distinct()
            .filter { it.isNotEmpty() && !it.isDigitsOnly() }
            .sorted()

    fun saveCountry(country: String) = informationStorage.saveCountry(country)

    fun saveStayInMainBuilding(stay: Boolean) = informationStorage.saveStayInJohanneshaus(stay)

    fun saveRoomOccupancy(position: Int) = informationStorage.saveRoomOccupancy(position)

    fun saveRoomBathroom(position: Int) = informationStorage.saveRoomBathroom(position)

    fun saveIsStudent(isStudent: Boolean) = informationStorage.saveIsStudent(isStudent)

    fun saveImageConsent(consent: Int) = informationStorage.saveImageConsent(consent)

    fun saveAddressConsent(consent: Int) = informationStorage.saveAddressConsent(consent)

    fun saveEatingHabit(isVegan: Boolean, isVegetarian: Boolean, allergens: List<String>) =
        informationStorage.saveEatingHabit(EatingHabit.create(isVegan, isVegetarian, allergens))

    /**
     * Persists a text field and advances the enrol-state machine. The REMIND/IN_PROGRESS
     * alternation looks odd but is what re-schedules the "continue enrolment" reminder to
     * fire ENROL_CONTINUE_REMINDER_OFFSET after the *latest* edit (every other change the
     * state falls back to REMIND, so the next change re-enters the scheduling branch).
     */
    fun onTextFieldChanged(fieldKey: String, inputText: String) {
        when (fieldKey) {
            FIELD_NAME -> informationStorage.saveName(inputText)
            FIELD_FIRSTNAME -> informationStorage.saveFirstName(inputText)
            FIELD_STREET_ADDRESS -> informationStorage.saveStreet(inputText)
            FIELD_POSTAL -> informationStorage.savePostal(inputText)
            FIELD_CITY -> informationStorage.saveCity(inputText)
            FIELD_PHONE -> informationStorage.savePhone(inputText)
            FIELD_MAIL -> informationStorage.saveMail(inputText)
            FIELD_YEARS_LATIN -> {
                inputText.toFloatOrNull()?.let { informationStorage.saveYearsOfLatin(it) }
            }
            FIELD_INSTRUMENT -> informationStorage.saveInstrument(inputText)
            FIELD_ROOM_REMARKS -> informationStorage.saveRoomRemarks(inputText)
            FIELD_AGE -> informationStorage.saveAge(inputText.toIntOrNull() ?: 0)
        }
        if (inputText.isNotEmpty()) {
            val currentState = informationStorage.loadEnrolState()
            informationStorage.saveEnrolState(ENROLLED_STATE_REMIND)
            if (currentState != ENROLLED_STATE_IN_PROGRESS) {
                informationStorage.saveEnrolState(ENROLLED_STATE_IN_PROGRESS)
                if (currentState != ENROLLED_STATE_NOT_ASK_AGAIN) {
                    alarmScheduler.scheduleAlarm(Calendar.getInstance().also {
                        it.add(
                            Calendar.DAY_OF_MONTH,
                            NotificationHelper.ENROL_CONTINUE_REMINDER_OFFSET.first
                        )
                        it.add(
                            Calendar.HOUR_OF_DAY,
                            NotificationHelper.ENROL_CONTINUE_REMINDER_OFFSET.second
                        )
                        it.add(
                            Calendar.MINUTE,
                            NotificationHelper.ENROL_CONTINUE_REMINDER_OFFSET.third
                        )
                    }, notificationHelper.pendingIntentForContinueEnrolReminder())
                }
            }
        }
    }

    /**
     * Assembles the enrolment email. The [context] is only used for string/array resources
     * within this call and is not retained. The template `enrol_send_email_template` declares
     * exactly 18 format args (incl. `%d` age and `%.1f` years of Latin) checked by lint —
     * keep the argument list in sync with it.
     */
    fun buildEnrolmentEmail(context: Context): EmailData {
        val info = informationStorage.loadEnrolInformation()

        val year = eventInfoStorage.loadSeptimanaStartEndTime()?.first?.let {
            SimpleDateFormat("yyyy", Locale.GERMAN).format(it.time)
        } ?: ""
        val subject =
            context.getString(R.string.enrol_send_email_subject, year, info.name, info.firstname)

        val septimanaLocation = eventInfoStorage.loadSeptimanaLocation()
        val body = context.getString(
            R.string.enrol_send_email_template,
            info.firstname,
            info.name,
            info.street,
            info.postal,
            info.city,
            info.country,
            info.phone,
            info.mail,
            context.getString(if (septimanaLocation == SeptimanaLocation.BRAUNFELS) R.string.enrol_send_email_hoehenblick else R.string.enrol_send_email_johanneshaus),
            context.getString(if (info.stayInMainBuilding) R.string.enrol_send_yes else R.string.enrol_send_no),
            buildRoomString(context, info),
            info.age,
            info.yearsOfLatin,
            context.getString(if (info.isStudent) R.string.enrol_send_yes else R.string.enrol_send_no),
            (info.eatingHabit ?: EatingHabit.create(
                isVegan = false,
                isVegetarian = false,
                allergens = Collections.emptyList()
            )).information(context),
            info.instrument,
            context.getString(
                when (info.imageConsent) {
                    ACCEPT_STATE_YES -> R.string.enrol_send_yes
                    else -> R.string.enrol_send_no
                }
            ),
            context.getString(
                when (info.addressConsent) {
                    ACCEPT_STATE_YES -> R.string.enrol_send_yes
                    else -> R.string.enrol_send_no
                }
            )
        )
        return EmailData(context.getString(R.string.enrol_send_email_address), subject, body)
    }

    /**
     * Builds the German room-category line for the enrolment email from the
     * (always German) email arrays. The "keine Angabe" entry at index 0 is dropped;
     * free-text remarks are appended in parentheses. Falls back to "keine Angabe" if
     * nothing is selected and no remarks are given.
     */
    private fun buildRoomString(context: Context, info: EnrolInformation): String {
        val occupancyOptions = context.resources.getStringArray(R.array.enrol_room_occupancy_email)
        val bathroomOptions = context.resources.getStringArray(R.array.enrol_room_bathroom_email)
        val parts = mutableListOf<String>()
        if (info.roomOccupancy in 1 until occupancyOptions.size) {
            parts.add(occupancyOptions[info.roomOccupancy])
        }
        if (info.roomBathroom in 1 until bathroomOptions.size) {
            parts.add(bathroomOptions[info.roomBathroom])
        }
        if (info.roomRemarks.isNotBlank()) {
            parts.add("(${info.roomRemarks.trim()})")
        }
        return if (parts.isEmpty()) occupancyOptions[0] else parts.joinToString(", ")
    }

    fun onEnrolmentSent() {
        informationStorage.saveEnrolState(ENROLLED_STATE_ENROLLED)
    }

    data class EmailData(val address: String, val subject: String, val body: String)

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_FIRSTNAME = "firstname"
        const val FIELD_STREET_ADDRESS = "street"
        const val FIELD_POSTAL = "postal"
        const val FIELD_CITY = "city"
        const val FIELD_PHONE = "phone"
        const val FIELD_MAIL = "mail"
        const val FIELD_YEARS_LATIN = "years_latin"
        const val FIELD_INSTRUMENT = "instrument"
        const val FIELD_ROOM_REMARKS = "room_remarks"
        const val FIELD_AGE = "age"
    }
}
