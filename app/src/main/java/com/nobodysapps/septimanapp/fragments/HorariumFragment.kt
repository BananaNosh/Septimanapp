package com.nobodysapps.septimanapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.dialog.MessageAndCheckboxDialogFragment
import com.nobodysapps.septimanapp.dialog.OutdatedHorariumDialogFragment
import com.nobodysapps.septimanapp.localization.localizedDisplayLanguage
import com.nobodysapps.septimanapp.viewModel.HorariumViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_horarium.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [HorariumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HorariumFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: HorariumViewModel

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var actionDayViewId = -1
    private var actionToggleHorariumLanguageId = -1


    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HorariumViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_horarium, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        setupHorariumView(landscape)
    }

    private fun setupHorariumView(landscape: Boolean) {
        // Get a reference for the week view in the layout.
        horariumView.changeOrientation(landscape)
        viewModel.horarium.observe(viewLifecycleOwner, androidx.lifecycle.Observer { horarium ->
            if (horarium != null) {
                horariumView.setHorarium(horarium)
            } else {
                onNoHorariumFound()
            }
        })
    }

    @SuppressLint("WrongConstant")
    private fun onNoHorariumFound() {
        view?.let {
            if (viewModel.hasPreviousHorarium()) {
                viewModel.usePreviousHorarium()
                if (viewModel.shouldShowWarning) {
                    val dialog = OutdatedHorariumDialogFragment()
                    dialog.listener = object : MessageAndCheckboxDialogFragment.Listener {
                        override fun onOkClicked(isChecked: Boolean) {
                            viewModel.shouldShowWarning = !isChecked
                        }
                    }
                    dialog.setTargetFragment(this, 0)
                    activity?.supportFragmentManager?.let { manager ->
                        val dialogTag = "NoHorarium"
                        val prevDialog = manager.findFragmentByTag(dialogTag)
                        if (prevDialog != null && (prevDialog as OutdatedHorariumDialogFragment).showsDialog) {
                            prevDialog.dismiss()
                        }
                        dialog.show(manager, dialogTag)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        actionDayViewId = getActionId(menu, actionDayViewId)
        val actionDayTitle = getToggleDayViewActionStringFromView()
        val itemDay = menu.add(Menu.NONE, actionDayViewId, 10, actionDayTitle)
        itemDay?.setIcon(getToggleDayViewActionIconResFromView())
        itemDay?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        actionToggleHorariumLanguageId = getActionId(menu, actionToggleHorariumLanguageId)
        val actionLanguageTitle = getToggleHorariumLanguageActionTitle()
        val itemLanguage =
            menu.add(Menu.NONE, actionToggleHorariumLanguageId, 11, actionLanguageTitle)
        itemLanguage?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getToggleHorariumLanguageActionTitle(): String {
        return getString(
            R.string.action_horarium_language,
            localizedDisplayLanguage(context, viewModel.toggledHorariumLocale())
        )
    }

    private fun getActionId(menu: Menu, actionId: Int): Int {
        var id = actionId
        if (id < 0) {
            id = 1
            while (menu.findItem(id) != null) {
                id++
            }
        }
        return id
    }

    private fun getToggleDayViewActionStringFromView() =
        resources.getQuantityString(
            R.plurals.action_day_view,
            horariumView.daysToShowOnToggleDayView,
            horariumView.daysToShowOnToggleDayView
        )

    private fun getToggleDayViewActionIconResFromView() =
        when (horariumView.daysToShowOnToggleDayView) {
            1 -> R.drawable.ic_view_day
            else -> R.drawable.ic_view_multiple_days
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            actionDayViewId -> {
                horariumView.toggleDayView()
                item.title = getToggleDayViewActionStringFromView()
                item.setIcon(getToggleDayViewActionIconResFromView())
            }
            actionToggleHorariumLanguageId -> {
                viewModel.toggleHorariumLanguage()
                item.title = getToggleHorariumLanguageActionTitle()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        const val SHOW_AGAIN_KEY = "show_again"

        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment HorariumFragment.
         */
        @JvmStatic
        fun newInstance() = HorariumFragment()
    }
}
