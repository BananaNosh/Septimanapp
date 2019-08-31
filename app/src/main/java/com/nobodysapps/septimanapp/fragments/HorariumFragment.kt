package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.SeptimanappActivity
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import kotlinx.android.synthetic.main.fragment_horarium.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [HorariumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HorariumFragment : Fragment() {
    @Inject
    lateinit var horariumStorage: HorariumStorage

    var actionDayViewId = -1


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
        val horarium = horariumStorage.loadHorarium(2018, "la")//TODO
        if (horarium != null) {
            horariumView.setHorarium(horarium)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (actionDayViewId < 0) {
            var id = 1
            while (menu.findItem(id) != null) {
                id++
            }
            actionDayViewId = id
        }
        val actionTitle = getToggleDayViewActionStringFromView()
        val item = menu.add(Menu.NONE, actionDayViewId, 10, actionTitle)
        item?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getToggleDayViewActionStringFromView() =
        resources.getQuantityString(R.plurals.action_day_view, horariumView.daysToShowOnToggleDayView, horariumView.daysToShowOnToggleDayView)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == actionDayViewId) {
            horariumView.toggleDayView()
            item.title = getToggleDayViewActionStringFromView()
            Log.d("HorariumFragment", "day clicked")
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
