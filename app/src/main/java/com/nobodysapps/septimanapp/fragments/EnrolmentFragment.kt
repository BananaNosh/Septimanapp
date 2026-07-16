package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.databinding.FragmentEnrolmentBinding
import com.nobodysapps.septimanapp.view.applySystemBarInsetsAsPadding
import com.nobodysapps.septimanapp.dialog.ConfirmEnrolmentDialogFragment
import com.nobodysapps.septimanapp.dialog.MessageAndCheckboxDialogFragment
import com.nobodysapps.septimanapp.model.EatingHabit
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_NO
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_NONE
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_YES
import com.nobodysapps.septimanapp.model.Vegan
import com.nobodysapps.septimanapp.model.Vegetarian
import com.nobodysapps.septimanapp.model.storage.SeptimanaLocation
import com.nobodysapps.septimanapp.viewModel.EnrolmentViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [EnrolmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EnrolmentFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: EnrolmentViewModel

    private var _binding: FragmentEnrolmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory)[EnrolmentViewModel::class.java]
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
            when (viewModel.septimanaLocation) {
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
                adapter.addAll(viewModel.countryList())
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                binding.enrolCountrySpinner.adapter = adapter
                binding.enrolCountrySpinner.setSelection(adapter.getPosition(Locale.GERMANY.displayCountry))
            }
        }
    }

    private fun loadForm() {
        val info = viewModel.loadEnrolInformation()
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
        binding.enrolNameEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_NAME))
        binding.enrolFirstameEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_FIRSTNAME))
        binding.enrolStreetEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_STREET_ADDRESS))
        binding.enrolPostalEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_POSTAL))
        binding.enrolCityEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_CITY))
        binding.enrolPhoneEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_PHONE))
        binding.enrolMailEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_MAIL))
        binding.enrolYearsLatinEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_YEARS_LATIN))
        binding.enrolInstrumentEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_INSTRUMENT))
        binding.enrolRoomRemarksEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_ROOM_REMARKS))
        binding.enrolAgeEdit.addTextChangedListener(EditTextListener(EnrolmentViewModel.FIELD_AGE))

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
                viewModel.saveCountry(country)
            }
        }

        binding.enrolJohanneshausCB.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveStayInMainBuilding(isChecked)
        }

        binding.enrolRoomOccupancySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    viewModel.saveRoomOccupancy(position)
                }
            }

        binding.enrolRoomBathroomSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    viewModel.saveRoomBathroom(position)
                }
            }

        binding.enrolStudentCB.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveIsStudent(isChecked)
        }

        setupEatingHabitListeners()
        val onImageConsentChangedLambda: (CompoundButton, Boolean) -> Unit = { btn, _ ->
            if (btn.isPressed) {
                when (btn) {
                    binding.enrolImageConsentYesRB -> viewModel.saveImageConsent(
                        ACCEPT_STATE_YES
                    )
                    binding.enrolImageConsentNoRB -> viewModel.saveImageConsent(
                        ACCEPT_STATE_NO
                    )
                    else -> viewModel.saveImageConsent(
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
                    binding.enrolAddressConsentYesRB -> viewModel.saveAddressConsent(
                        ACCEPT_STATE_YES
                    )
                    binding.enrolAddressConsentNoRB -> viewModel.saveAddressConsent(
                        ACCEPT_STATE_NO
                    )
                    else -> viewModel.saveAddressConsent(
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
            viewModel.saveEatingHabit(
                binding.enrolVeganCB.isChecked,
                binding.enrolVegetarianCB.isChecked,
                allergens
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
        val enrolInformation = viewModel.loadEnrolInformation()
        if (!enrolInformation.isValid()) {
            val missingFields = enrolInformation.missingFields().map { getString(it.labelRes) }
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

    private fun sendEnrolment() {
        val context = context ?: return
        val email = viewModel.buildEnrolmentEmail(context)
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.address))
            putExtra(Intent.EXTRA_SUBJECT, email.subject)
            putExtra(Intent.EXTRA_TEXT, email.body)
        }
        startActivity(emailIntent)
        viewModel.onEnrolmentSent()
    }

    companion object {
        const val TAG = "EnrolmentFragment"

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
            viewModel.onTextFieldChanged(fieldKey, s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}