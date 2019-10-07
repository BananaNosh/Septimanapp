package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.SeptimanappActivity
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import kotlinx.android.synthetic.main.fragment_enrolment.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [EnrolmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EnrolmentFragment : Fragment() {
    @Inject
    lateinit var informationStorage: EnrolInformationStorage

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
        loadForm()
        setupListeners()
    }

    private fun loadForm() {
        val (name, firstname) = informationStorage.loadEnrolInformation()
        enrollNameEdit.setText(name)
        enrollFirstameEdit.setText(firstname)
    }

    private fun setupListeners() {
        val nameEditTextListener = EditTextListener(FIELD_NAME)
        enrollNameEdit.addTextChangedListener(nameEditTextListener)
        val firstnameEditTextListener = EditTextListener(FIELD_FIRSTNAME)
        enrollFirstameEdit.addTextChangedListener(firstnameEditTextListener)

        fabEnrolSend.setOnClickListener {
            sendEnrolment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!(context is SeptimanappActivity)) {
            throw RuntimeException("$context must inherit from SeptimanappActivity")
        }
        context.getSeptimanappApplication().component.inject(this)
    }

    private fun sendEnrolment() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        val aEmailList = arrayOf("user@fakehost.com", "user2@fakehost.com")
        val aEmailCCList = arrayOf("user3@fakehost.com", "user4@fakehost.com")
        val aEmailBCCList = arrayOf("user5@fakehost.com")

        emailIntent.putExtra(Intent.EXTRA_EMAIL, aEmailList)
        emailIntent.putExtra(Intent.EXTRA_CC, aEmailCCList)
        emailIntent.putExtra(Intent.EXTRA_BCC, aEmailBCCList)

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My subject")

        emailIntent.type = "plain/text"
        emailIntent.putExtra(Intent.EXTRA_TEXT, "My message body.")

        startActivity(emailIntent)
    }

    companion object {
        const val TAG = "EnrolmentFragment"

        const val ENROLLED_STATE_KEY = "enrolled_state"

        const val ENROLLED_STATE_REMIND = 0
        const val ENROLLED_STATE_ENROLLED = 1
        const val ENROLLED_STATE_IN_PROGRESS = 2
        const val ENROLLED_STATE_NOT_ASK_AGAIN = 3

        private const val FIELD_NAME = "name"
        private const val FIELD_FIRSTNAME = "firstname"

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
                FIELD_FIRSTNAME -> informationStorage.saveFirstame(inputText)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }
}
