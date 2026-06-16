package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.databinding.FragmentEnrolmentBinding
import com.nobodysapps.septimanapp.view.applySystemBarInsetsAsPadding
import com.nobodysapps.septimanapp.dialog.ConfirmEnrolmentDialogFragment
import com.nobodysapps.septimanapp.dialog.MessageAndCheckboxDialogFragment
import com.nobodysapps.septimanapp.model.EatingHabit
import com.nobodysapps.septimanapp.model.EnrolInformation
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_NO
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_NONE
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_YES
import com.nobodysapps.septimanapp.model.Vegan
import com.nobodysapps.septimanapp.model.Vegetarian
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
import dagger.android.support.AndroidSupportInjection
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [EnrolmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EnrolmentFragment : Fragment() {
    @Inject
    lateinit var informationStorage: EnrolInformationStorage

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var eventInfoStorage: EventInfoStorage

    private var _binding: FragmentEnrolmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEnrolmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Unlike the map/horarium (which bleed to the edges), keep the form fields and
        // the Send FAB clear of the bottom nav bar and the landscape camera / side nav bar.
        binding.root.applySystemBarInsetsAsPadding(bottom = true, horizontal = true)

        binding.enrolJohanneshausCB.setText(
            when (eventInfoStorage.loadSeptimanaLocation()) {
                SeptimanaLocation.AMOENEBURG -> R.string.enrol_checkbox_johannes_haus
                SeptimanaLocation.BRAUNFELS -> R.string.enrol_checkbox_hoehenblick
            }
        )
        fillSpinner()
        setupListeners()
        loadForm()
    }

    private fun fillSpinner() {
        context?.let { context ->
            ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).also { adapter ->
                val locales: Array<Locale> = Locale.getAvailableLocales()
                val localCountries = ArrayList<String>()
                for (l in locales) {
                    localCountries.add(l.displayCountry)
                }
                adapter.addAll(
                    localCountries.distinct().filter { it.isNotEmpty() && !it.isDigitsOnly() }
                        .sorted()
                )
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                binding.enrolCountrySpinner.adapter = adapter
                binding.enrolCountrySpinner.setSelection(adapter.getPosition(Locale.GERMANY.displayCountry))
            }
        }
    }

    private fun loadForm() {
        val info = informationStorage.loadEnrolInformation()
        val (name, firstname, street, postal, city, country, phone, mail, stayInJohannesHaus, yearsOfLatin, eatingHabit, instrument, imageConsent, addressConsent) = info
        binding.enrolNameEdit.setText(name)
        binding.enrolFirstameEdit.setText(firstname)
        binding.enrolStreetEdit.setText(street)
        binding.enrolPostalEdit.setText(postal)
        binding.enrolCityEdit.setText(city)
        binding.enrolPhoneEdit.setText(phone)
        binding.enrolMailEdit.setText(mail)
        binding.enrolInstrumentEdit.setText(instrument)

        binding.enrolRoomOccupancySpinner.setSelection(info.roomOccupancy)
        binding.enrolRoomBathroomSpinner.setSelection(info.roomBathroom)
        binding.enrolRoomRemarksEdit.setText(info.roomRemarks)
        if (info.age > 0) {
            binding.enrolAgeEdit.setText(info.age.toString())
        }
        binding.enrolStudentCB.isChecked = info.isStudent

        binding.enrolJohanneshausCB.isChecked = stayInJohannesHaus
        binding.enrolImageConsentYesRB.isChecked = imageConsent == ACCEPT_STATE_YES
        binding.enrolImageConsentNoRB.isChecked = imageConsent == ACCEPT_STATE_NO
        binding.enrolAddressConsentYesRB.isChecked = addressConsent == ACCEPT_STATE_YES
        binding.enrolAddressConsentNoRB.isChecked = addressConsent == ACCEPT_STATE_NO

        if (yearsOfLatin > 0) {
            binding.enrolYearsLatinEdit.setText(
                if (yearsOfLatin.toInt().toFloat() == yearsOfLatin) yearsOfLatin.toInt()
                    .toString() else yearsOfLatin.toString()
            )
        }

        @Suppress("UNCHECKED_CAST") val adapter =
            binding.enrolCountrySpinner.adapter as? ArrayAdapter<String>
        if (adapter != null) {
            val selectedCountry = if (country.isEmpty()) Locale.GERMANY.displayCountry else country
            binding.enrolCountrySpinner.setSelection(adapter.getPosition(selectedCountry))
        }

        fillCheckboxesFromEatingHabit(eatingHabit)
    }

    private fun fillCheckboxesFromEatingHabit(eatingHabit: EatingHabit?) {
        if (eatingHabit != null && context != null) {
            if (eatingHabit is Vegan) {
                binding.enrolVeganCB.isChecked = true
            }
            if (eatingHabit is Vegetarian) {
                binding.enrolVegetarianCB.isChecked = true
            }
            val allergens = eatingHabit.allergens.toMutableList()
            val glutenStr = requireContext().getString(R.string.eating_habit_gluten)
            if (glutenStr in allergens) {
                binding.enrolGlutenfreeCB.isChecked = true
                allergens.remove(glutenStr)
            }
            binding.enrolAllergensEdit.setText(allergens.map { it.trim().replace(",", "") }.joinToString { it })
            binding.enrolAllergensCB.isChecked = allergens.any { it.isNotEmpty() }
        }
    }

    private fun setupListeners() {
        val nameEditTextListener = EditTextListener(FIELD_NAME)
        binding.enrolNameEdit.addTextChangedListener(nameEditTextListener)
        val firstnameEditTextListener = EditTextListener(FIELD_FIRSTNAME)
        binding.enrolFirstameEdit.addTextChangedListener(firstnameEditTextListener)
        val streetAddressEditTextListener = EditTextListener(FIELD_STREET_ADDRESS)
        binding.enrolStreetEdit.addTextChangedListener(streetAddressEditTextListener)
        val postalEditTextListener = EditTextListener(FIELD_POSTAL)
        binding.enrolPostalEdit.addTextChangedListener(postalEditTextListener)
        val cityEditTextListener = EditTextListener(FIELD_CITY)
        binding.enrolCityEdit.addTextChangedListener(cityEditTextListener)
        val phoneEditTextListener = EditTextListener(FIELD_PHONE)
        binding.enrolPhoneEdit.addTextChangedListener(phoneEditTextListener)
        val mailEditTextListener = EditTextListener(FIELD_MAIL)
        binding.enrolMailEdit.addTextChangedListener(mailEditTextListener)
        val yearsOfLatinEditTextListener = EditTextListener(FIELD_YEARS_LATIN)
        binding.enrolYearsLatinEdit.addTextChangedListener(yearsOfLatinEditTextListener)
        val instrumentEditTextListener = EditTextListener(FIELD_INSTRUMENT)
        binding.enrolInstrumentEdit.addTextChangedListener(instrumentEditTextListener)
        val roomRemarksEditTextListener = EditTextListener(FIELD_ROOM_REMARKS)
        binding.enrolRoomRemarksEdit.addTextChangedListener(roomRemarksEditTextListener)
        val ageEditTextListener = EditTextListener(FIELD_AGE)
        binding.enrolAgeEdit.addTextChangedListener(ageEditTextListener)

        binding.enrolYearsLatinEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val yearsBackString = try {
                    val yearsOfLatin = s.toString().toFloat()
                    resources.getQuantityString(
                        R.plurals.enrol_years_of_latin_back,
                        if (yearsOfLatin == 1f) 1 else 2
                    )
                } catch (e: NumberFormatException) {
                    resources.getQuantityString(R.plurals.enrol_years_of_latin_back, 0)
                }
                binding.enrolYearsLatinBackTV.text = yearsBackString
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.enrolCountrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(position)
                val country = item.toString()
                informationStorage.saveCountry(country)
            }
        }

        binding.enrolJohanneshausCB.setOnCheckedChangeListener { _, isChecked ->
            informationStorage.saveStayInJohanneshaus(isChecked)
        }

        binding.enrolRoomOccupancySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    informationStorage.saveRoomOccupancy(position)
                }
            }

        binding.enrolRoomBathroomSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    informationStorage.saveRoomBathroom(position)
                }
            }

        binding.enrolStudentCB.setOnCheckedChangeListener { _, isChecked ->
            informationStorage.saveIsStudent(isChecked)
        }

        setupEatingHabitListeners()
        val onImageConsentChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, _ ->
            if (btn.isPressed) {
                when (btn) {
                    binding.enrolImageConsentYesRB -> informationStorage.saveImageConsent(
                        ACCEPT_STATE_YES
                    )
                    binding.enrolImageConsentNoRB -> informationStorage.saveImageConsent(
                        ACCEPT_STATE_NO
                    )
                    else -> informationStorage.saveImageConsent(
                        ACCEPT_STATE_NONE
                    )
                }
            }
        }
        binding.enrolImageConsentYesRB.setOnCheckedChangeListener(onImageConsentChangedLambda)
        binding.enrolImageConsentNoRB.setOnCheckedChangeListener(onImageConsentChangedLambda)

        val onAddressConsentChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, _ ->
            if (btn.isPressed) {
                when (btn) {
                    binding.enrolAddressConsentYesRB -> informationStorage.saveAddressConsent(
                        ACCEPT_STATE_YES
                    )
                    binding.enrolAddressConsentNoRB -> informationStorage.saveAddressConsent(
                        ACCEPT_STATE_NO
                    )
                    else -> informationStorage.saveAddressConsent(
                        ACCEPT_STATE_NONE
                    )
                }
            }
        }
        binding.enrolAddressConsentYesRB.setOnCheckedChangeListener(onAddressConsentChangedLambda)
        binding.enrolAddressConsentNoRB.setOnCheckedChangeListener(onAddressConsentChangedLambda)

        binding.enrolInstrumentEdit.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND
                || actionId == EditorInfo.IME_NULL && event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                showConfirmDialog()
            }
            true
        }

        binding.fabEnrolSend.setOnClickListener {
            showConfirmDialog()
        }
    }

    private fun setupEatingHabitListeners() {
        val onEatingHabitChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, isChecked ->
            val allBtns = listOf<CompoundButton>(
                binding.enrolEverythingCB,
                binding.enrolGlutenfreeCB,
                binding.enrolVegetarianCB,
                binding.enrolVeganCB,
                binding.enrolAllergensCB
            )
            if (isChecked) {
                when (btn.id) {
                    R.id.enrolEverythingCB -> {
                        for (b in allBtns) {
                            if (b != btn) {
                                b.isChecked = false
                            }
                        }
                    }
                    R.id.enrolVeganCB -> {
                        binding.enrolEverythingCB.isChecked = false
                        binding.enrolVegetarianCB.isChecked = true
                    }
                    R.id.enrolVegetarianCB -> {
                        binding.enrolEverythingCB.isChecked = false
                    }
                    R.id.enrolGlutenfreeCB -> binding.enrolEverythingCB.isChecked = false
                    R.id.enrolAllergensCB -> binding.enrolEverythingCB.isChecked = false
                }
            } else if (btn.id == R.id.enrolVegetarianCB) {
                binding.enrolVeganCB.isChecked = false
            }
            val allergens = ArrayList<String>()
            if (binding.enrolGlutenfreeCB.isChecked && context != null) {
                allergens.add(requireContext().getString(R.string.eating_habit_gluten))
            }
            if (binding.enrolAllergensCB.isChecked) {
                allergens.addAll(binding.enrolAllergensEdit.text.split(" "))
            }
            informationStorage.saveEatingHabit(
                EatingHabit.create(
                    binding.enrolVeganCB.isChecked,
                    binding.enrolVegetarianCB.isChecked,
                    allergens
                )
            )
        }
        binding.enrolVeganCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        binding.enrolVegetarianCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        binding.enrolGlutenfreeCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        binding.enrolEverythingCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        binding.enrolAllergensCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        binding.enrolAllergensEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.enrolAllergensCB.isChecked = true
                onEatingHabitChangedLambda(
                    binding.enrolAllergensCB,
                    true
                )  // needed as otherwise only for the first letter the onCheckedChange is called
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    private fun showConfirmDialog() {
        this.activity?.currentFocus?.let { view ->
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
        val enrolInformation = informationStorage.loadEnrolInformation()
        if (!enrolInformation.isValid()) {
            val missingFields = missingFieldLabels(enrolInformation)
            view?.let {
                val message = if (missingFields.isEmpty()) {
                    getString(R.string.enrol_not_all_data_given)
                } else {
                    getString(R.string.enrol_missing_fields, missingFields.joinToString(", "))
                }
                Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
            }
            return
        }
        activity?.supportFragmentManager?.let {
            val confirmDialog = ConfirmEnrolmentDialogFragment()
            confirmDialog.listener = object : MessageAndCheckboxDialogFragment.Listener {
                override fun onOkClicked(isChecked: Boolean) {
                    sendEnrolment()
                }
            }
            confirmDialog.show(it, "Confirm")
        }
    }

    /**
     * Returns the localized labels of the required fields that are still missing, so the
     * snackbar can tell the user exactly what to complete. Kept in sync with
     * [EnrolInformation.isValid].
     */
    private fun missingFieldLabels(info: EnrolInformation): List<String> {
        val missing = mutableListOf<String>()
        if (info.name.isBlank()) missing.add(getString(R.string.enrol_last_name_hint))
        if (info.firstname.isBlank()) missing.add(getString(R.string.enrol_first_name_hint))
        if (info.street.isBlank()) missing.add(getString(R.string.enrol_street_hint))
        if (info.postal.isBlank()) missing.add(getString(R.string.enrol_postal_code_hint))
        if (info.city.isBlank()) missing.add(getString(R.string.enrol_city_hint))
        if (info.country.isBlank()) missing.add(getString(R.string.enrol_country_hint))
        if (info.phone.isBlank()) missing.add(getString(R.string.enrol_phone_hint))
        if (info.mail.isBlank()) missing.add(getString(R.string.enrol_mail_hint))
        if (info.age <= 0) missing.add(getString(R.string.enrol_age_label))
        if (info.addressConsent == ACCEPT_STATE_NONE) {
            missing.add(getString(R.string.enrol_field_address_consent))
        }
        if (info.imageConsent == ACCEPT_STATE_NONE) {
            missing.add(getString(R.string.enrol_field_image_consent))
        }
        return missing
    }

    private fun sendEnrolment() {
        val info = informationStorage.loadEnrolInformation()
        val (name, firstname, street, postal, city, country, phone, mail, stayInMainBuilding, yearsOfLatin, eatingHabit, instrument, imageConsent, addressConsent) = info

        val emailIntent = Intent(Intent.ACTION_SEND)
        val aEmailList = arrayOf(getString(R.string.enrol_send_email_address))

        emailIntent.putExtra(Intent.EXTRA_EMAIL, aEmailList)

        val year = eventInfoStorage.loadSeptimanaStartEndTime()?.first?.let {
            SimpleDateFormat(
                "yyyy",
                Locale.GERMAN
            ).format(it.time)
        } ?: ""
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.enrol_send_email_subject, year, name, firstname)
        )

        val septimanaLocation = eventInfoStorage.loadSeptimanaLocation()

        emailIntent.type = "plain/text"
        if (context != null) {
            val body = getString(
                R.string.enrol_send_email_template,
                firstname,
                name,
                street,
                postal,
                city,
                country,
                phone,
                mail,
                getString(if (septimanaLocation == SeptimanaLocation.BRAUNFELS) R.string.enrol_send_email_hoehenblick else R.string.enrol_send_email_johanneshaus),
                getString(if (stayInMainBuilding) R.string.enrol_send_yes else R.string.enrol_send_no),
                buildRoomString(info),
                info.age,
                yearsOfLatin,
                getString(if (info.isStudent) R.string.enrol_send_yes else R.string.enrol_send_no),
                (eatingHabit ?: EatingHabit.create(
                    isVegan = false,
                    isVegetarian = false,
                    allergens = Collections.emptyList()
                )).information(requireContext()),
                instrument,
                getString(
                    when (imageConsent) {
                        ACCEPT_STATE_YES -> R.string.enrol_send_yes
                        else -> R.string.enrol_send_no
                    }
                ),
                getString(
                    when (addressConsent) {
                        ACCEPT_STATE_YES -> R.string.enrol_send_yes
                        else -> R.string.enrol_send_no
                    }
                )
            )
            emailIntent.putExtra(Intent.EXTRA_TEXT, body)

            startActivity(emailIntent)
            resetReminderNotifications()
        }
    }

    /**
     * Builds the German room-category line for the enrolment email from the
     * (always German) email arrays. The "keine Angabe" entry at index 0 is dropped;
     * free-text remarks are appended in parentheses. Falls back to "keine Angabe" if
     * nothing is selected and no remarks are given.
     */
    private fun buildRoomString(info: EnrolInformation): String {
        val occupancyOptions = resources.getStringArray(R.array.enrol_room_occupancy_email)
        val bathroomOptions = resources.getStringArray(R.array.enrol_room_bathroom_email)
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

    private fun resetReminderNotifications() {
        informationStorage.saveEnrolState(ENROLLED_STATE_ENROLLED)
    }

    companion object {
        const val TAG = "EnrolmentFragment"

        private const val FIELD_NAME = "name"
        private const val FIELD_FIRSTNAME = "firstname"
        private const val FIELD_STREET_ADDRESS = "street"
        private const val FIELD_POSTAL = "postal"
        private const val FIELD_CITY = "city"
        private const val FIELD_PHONE = "phone"
        private const val FIELD_MAIL = "mail"
        private const val FIELD_YEARS_LATIN = "years_latin"
        private const val FIELD_INSTRUMENT = "instrument"
        private const val FIELD_ROOM_REMARKS = "room_remarks"
        private const val FIELD_AGE = "age"

        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment HorariumFragment.
         */
        @JvmStatic
        fun newInstance() = EnrolmentFragment()
    }

    private inner class EditTextListener(private val fieldKey: String) : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            val inputText = s.toString()
            when (fieldKey) {
                FIELD_NAME -> informationStorage.saveName(inputText)
                FIELD_FIRSTNAME -> informationStorage.saveFirstName(inputText)
                FIELD_STREET_ADDRESS -> informationStorage.saveStreet(inputText)
                FIELD_POSTAL -> informationStorage.savePostal(inputText)
                FIELD_CITY -> informationStorage.saveCity(inputText)
                FIELD_PHONE -> informationStorage.savePhone(inputText)
                FIELD_MAIL -> informationStorage.saveMail(inputText)
                FIELD_YEARS_LATIN -> {
                    try {
                        val years = inputText.toFloat()
                        informationStorage.saveYearsOfLatin(years)
                    } catch (e: NumberFormatException) {
                    }
                }
                FIELD_INSTRUMENT -> informationStorage.saveInstrument(inputText)
                FIELD_ROOM_REMARKS -> informationStorage.saveRoomRemarks(inputText)
                FIELD_AGE -> informationStorage.saveAge(inputText.toIntOrNull() ?: 0)
            }
            if (inputText.isNotEmpty()) {
                val currentState = informationStorage.loadEnrolState()
                informationStorage.saveEnrolState(ENROLLED_STATE_REMIND)  // TODO why??
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

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}