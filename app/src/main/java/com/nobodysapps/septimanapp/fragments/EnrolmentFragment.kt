package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
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
        fabEnrolSend.setOnClickListener {

        }
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
        enrollNameEdit.setOnEditorActionListener(nameEditTextListener)
        val firstnameEditTextListener = EditTextListener(FIELD_FIRSTNAME)
        enrollFirstameEdit.addTextChangedListener(firstnameEditTextListener)
        enrollFirstameEdit.setOnEditorActionListener(firstnameEditTextListener)
        //        enrollNameEdit.addTextChangedListener(object: TextWatcher {
        //            override fun afterTextChanged(s: Editable?) {
        //                informationStorage.saveName(s.toString())
        //            }
        //
        //            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //            }
        //
        //            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //            }
        //
        //        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!(context is SeptimanappActivity)) {
            throw RuntimeException("$context must inherit from SeptimanappActivity")
        }
        context.getSeptimanappApplication().component.inject(this)
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

    private inner class EditTextListener(private val fieldKey: String) : TextWatcher, TextView.OnEditorActionListener {
        private fun saveText(text: String, saveFunction: (String)-> Unit, savedText: String) {
            if (!(text in savedText)) {
                saveFunction(text)
            }
        }

        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val text = v?.text.toString() ?: ""
                when (fieldKey) {
                    FIELD_NAME -> informationStorage.saveName(text)
                    FIELD_FIRSTNAME -> informationStorage.saveFirstame(text)
                }
                return false;
            }
            return false;
        }

        override fun afterTextChanged(s: Editable?) {
            val (name, firstname) = informationStorage.loadEnrolInformation()
            val inputText = s.toString()
            when (fieldKey) {
                FIELD_NAME -> saveText(inputText, informationStorage::saveName, name)
                FIELD_FIRSTNAME -> saveText(inputText, informationStorage::saveFirstame, firstname)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }
}
