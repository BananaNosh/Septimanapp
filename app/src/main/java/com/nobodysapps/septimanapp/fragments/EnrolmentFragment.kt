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
import com.nobodysapps.septimanapp.dialog.ConfirmEnrolmentDialogFragment
import com.nobodysapps.septimanapp.dialog.MessageAndCheckboxDialogFragment
import com.nobodysapps.septimanapp.model.EatingHabit
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
import kotlinx.android.synthetic.main.fragment_enrolment.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enrolment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enrolJohanneshausCB.setText(
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
                enrolCountrySpinner.adapter = adapter
                enrolCountrySpinner.setSelection(adapter.getPosition(Locale.GERMANY.displayCountry))
            }
        }
    }

    private fun loadForm() {
        val (name, firstname, street, postal, city, country, phone, mail, stayInJohannesHaus, yearsOfLatin, eatingHabit, instrument, imageConsent, addressConsent) = informationStorage.loadEnrolInformation()
        enrolNameEdit.setText(name)
        enrolFirstameEdit.setText(firstname)
        enrolStreetEdit.setText(street)
        enrolPostalEdit.setText(postal)
        enrolCityEdit.setText(city)
        enrolPhoneEdit.setText(phone)
        enrolMailEdit.setText(mail)
        enrolInstrumentEdit.setText(instrument)

        enrolJohanneshausCB.isChecked = stayInJohannesHaus
        enrolImageConsentYesRB.isChecked = imageConsent == ACCEPT_STATE_YES
        enrolImageConsentNoRB.isChecked = imageConsent == ACCEPT_STATE_NO
        enrolAddressConsentYesRB.isChecked = addressConsent == ACCEPT_STATE_YES
        enrolAddressConsentNoRB.isChecked = addressConsent == ACCEPT_STATE_NO

        if (yearsOfLatin > 0) {
            enrolYearsLatinEdit.setText(
                if (yearsOfLatin.toInt().toFloat() == yearsOfLatin) yearsOfLatin.toInt()
                    .toString() else yearsOfLatin.toString()
            )
        }

        @Suppress("UNCHECKED_CAST") val adapter =
            enrolCountrySpinner.adapter as? ArrayAdapter<String>
        if (adapter != null) {
            val selectedCountry = if (country.isEmpty()) Locale.GERMANY.displayCountry else country
            enrolCountrySpinner.setSelection(adapter.getPosition(selectedCountry))
        }

        fillCheckboxesFromEatingHabit(eatingHabit)
    }

    private fun fillCheckboxesFromEatingHabit(eatingHabit: EatingHabit?) {
        if (eatingHabit != null && context != null) {
            if (eatingHabit is Vegan) {
                enrolVeganCB.isChecked = true
            }
            if (eatingHabit is Vegetarian) {
                enrolVegetarianCB.isChecked = true
            }
            val allergens = eatingHabit.allergens.toMutableList()
            val glutenStr = requireContext().getString(R.string.eating_habit_gluten)
            if (glutenStr in allergens) {
                enrolGlutenfreeCB.isChecked = true
                allergens.remove(glutenStr)
            }
            enrolAllergensEdit.setText(allergens.map { it.trim().replace(",", "") }.joinToString { it })
            enrolAllergensCB.isChecked = allergens.any { it.isNotEmpty() }
        }
    }

    private fun setupListeners() {
        val nameEditTextListener = EditTextListener(FIELD_NAME)
        enrolNameEdit.addTextChangedListener(nameEditTextListener)
        val firstnameEditTextListener = EditTextListener(FIELD_FIRSTNAME)
        enrolFirstameEdit.addTextChangedListener(firstnameEditTextListener)
        val streetAddressEditTextListener = EditTextListener(FIELD_STREET_ADDRESS)
        enrolStreetEdit.addTextChangedListener(streetAddressEditTextListener)
        val postalEditTextListener = EditTextListener(FIELD_POSTAL)
        enrolPostalEdit.addTextChangedListener(postalEditTextListener)
        val cityEditTextListener = EditTextListener(FIELD_CITY)
        enrolCityEdit.addTextChangedListener(cityEditTextListener)
        val phoneEditTextListener = EditTextListener(FIELD_PHONE)
        enrolPhoneEdit.addTextChangedListener(phoneEditTextListener)
        val mailEditTextListener = EditTextListener(FIELD_MAIL)
        enrolMailEdit.addTextChangedListener(mailEditTextListener)
        val yearsOfLatinEditTextListener = EditTextListener(FIELD_YEARS_LATIN)
        enrolYearsLatinEdit.addTextChangedListener(yearsOfLatinEditTextListener)
        val instrumentEditTextListener = EditTextListener(FIELD_INSTRUMENT)
        enrolInstrumentEdit.addTextChangedListener(instrumentEditTextListener)

        enrolYearsLatinEdit.addTextChangedListener(object : TextWatcher {
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
                enrolYearsLatinBackTV.text = yearsBackString
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })


        enrolCountrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        enrolJohanneshausCB.setOnCheckedChangeListener { _, isChecked ->
            informationStorage.saveStayInJohanneshaus(isChecked)
        }

        setupEatingHabitListeners()
        val onImageConsentChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, _ ->
            if (btn.isPressed) {
                when (btn) {
                    enrolImageConsentYesRB -> informationStorage.saveImageConsent(
                        ACCEPT_STATE_YES
                    )
                    enrolImageConsentNoRB -> informationStorage.saveImageConsent(
                        ACCEPT_STATE_NO
                    )
                    else -> informationStorage.saveImageConsent(
                        ACCEPT_STATE_NONE
                    )
                }
            }
        }
        enrolAddressConsentYesRB.setOnCheckedChangeListener(onImageConsentChangedLambda)
        enrolAddressConsentNoRB.setOnCheckedChangeListener(onImageConsentChangedLambda)

        val onAddressConsentChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, _ ->
            if (btn.isPressed) {
                when (btn) {
                    enrolAddressConsentYesRB -> informationStorage.saveAddressConsent(
                        ACCEPT_STATE_YES
                    )
                    enrolAddressConsentNoRB -> informationStorage.saveAddressConsent(
                        ACCEPT_STATE_NO
                    )
                    else -> informationStorage.saveAddressConsent(
                        ACCEPT_STATE_NONE
                    )
                }
            }
        }
        enrolAddressConsentYesRB.setOnCheckedChangeListener(onAddressConsentChangedLambda)
        enrolAddressConsentNoRB.setOnCheckedChangeListener(onAddressConsentChangedLambda)

        enrolInstrumentEdit.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND
                || actionId == EditorInfo.IME_NULL && event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                showConfirmDialog()
            }
            true
        }

        fabEnrolSend.setOnClickListener {
            showConfirmDialog()
        }
    }

    private fun setupEatingHabitListeners() {
        val onEatingHabitChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, isChecked ->
            val allBtns = listOf<CompoundButton>(
                enrolEverythingCB,
                enrolGlutenfreeCB,
                enrolVegetarianCB,
                enrolVeganCB,
                enrolAllergensCB
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
                        enrolEverythingCB.isChecked = false
                        enrolVegetarianCB.isChecked = true
                    }
                    R.id.enrolVegetarianCB -> {
                        enrolEverythingCB.isChecked = false
                    }
                    R.id.enrolGlutenfreeCB -> enrolEverythingCB.isChecked = false
                    R.id.enrolAllergensCB -> enrolEverythingCB.isChecked = false
                }
            } else if (btn.id == R.id.enrolVegetarianCB) {
                enrolVeganCB.isChecked = false
            }
            val allergens = ArrayList<String>()
            if (enrolGlutenfreeCB.isChecked && context != null) {
                allergens.add(requireContext().getString(R.string.eating_habit_gluten))
            }
            if (enrolAllergensCB.isChecked) {
                allergens.addAll(enrolAllergensEdit.text.split(" "))
            }
            informationStorage.saveEatingHabit(
                EatingHabit.create(
                    enrolVeganCB.isChecked,
                    enrolVegetarianCB.isChecked,
                    allergens
                )
            )
        }
        enrolVeganCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        enrolVegetarianCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        enrolGlutenfreeCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        enrolEverythingCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        enrolAllergensCB.setOnCheckedChangeListener(onEatingHabitChangedLambda)
        enrolAllergensEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enrolAllergensCB.isChecked = true
                onEatingHabitChangedLambda(
                    enrolAllergensCB,
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
        if (!checkAllDataGiven()) {
            view?.let {
                Snackbar.make(it, getString(R.string.enrol_not_all_data_given), Snackbar.LENGTH_LONG).show()
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

    private fun checkAllDataGiven(): Boolean {
        val enrolInformation = informationStorage.loadEnrolInformation()
        return enrolInformation.isValid()
    }

    private fun sendEnrolment() {
        val (name, firstname, street, postal, city, country, phone, mail, stayInMainBuilding, yearsOfLatin, eatingHabit, instrument, imageConsent, addressConsent) = informationStorage.loadEnrolInformation()

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
                yearsOfLatin,
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
