package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.SeptimanappActivity
import com.nobodysapps.septimanapp.localization.localizedDisplayLanguage
import com.nobodysapps.septimanapp.model.Horarium
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import kotlinx.android.synthetic.main.fragment_horarium.*
import java.util.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [HorariumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HorariumFragment : Fragment() {
    @Inject
    lateinit var horariumStorage: HorariumStorage

    var horariumInLatin: Boolean = Locale.getDefault().language != "de"

    private var actionDayViewId = -1
    private var actionToggleHorariumLanguageId = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        val horarium = loadHorariumInCorrectLanguage()
        if (horarium != null) {
            horariumView.setHorarium(horarium)
        } else {
            onNoHorariumFound()
        }
    }

    private fun loadHorariumInCorrectLanguage(): Horarium? {
        val horariumLanguage = if (horariumInLatin) "la" else "de"
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return horariumStorage.loadHorarium(currentYear, horariumLanguage)
    }

    private fun onNoHorariumFound() {
        if (view != null) {
            Snackbar.make(view!!, R.string.snackbar_horarium_not_found, Snackbar.LENGTH_LONG).show()
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
            localizedDisplayLanguage(context, if (horariumInLatin) Locale.GERMAN else Locale("la"))
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
                horariumInLatin = !horariumInLatin
                val horarium = loadHorariumInCorrectLanguage()
                if (horarium != null) {
                    horariumView.setHorarium(horarium)
                } else {  // change back as horarium could not be loaded in other language
                    horariumInLatin = !horariumInLatin
                    onNoHorariumFound()
                }
                item.title = getToggleHorariumLanguageActionTitle()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!(context is SeptimanappActivity)) {
            throw RuntimeException("$context must inherit from SeptimanappActivity")
        }
        context.getSeptimanappApplication().component.inject(this)
    }


    companion object {
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
