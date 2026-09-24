package com.nobodysapps.septimanapp.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.databinding.FragmentHorariumBinding
import com.nobodysapps.septimanapp.dialog.ExportHorariumDialogFragment
import com.nobodysapps.septimanapp.dialog.MessageAndCheckboxDialogFragment
import com.nobodysapps.septimanapp.dialog.OutdatedHorariumDialogFragment
import com.nobodysapps.septimanapp.export.HorariumIcsExporter
import com.nobodysapps.septimanapp.localization.localizedDisplayLanguage
import com.nobodysapps.septimanapp.viewModel.HorariumViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.io.File
import java.io.IOException
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
    private var _binding: FragmentHorariumBinding? = null
    private val binding get() = _binding!!

    private var actionDayViewId = -1
    private var actionToggleHorariumLanguageId = -1
    private var actionExportHorariumId = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HorariumViewModel::class.java)
        // The dialogs are shown as child fragments, which are restored together with this fragment,
        // so after a rotation their results still reach these listeners.
        childFragmentManager.setFragmentResultListener(
            ExportHorariumDialogFragment.REQUEST_KEY, this
        ) { _, result ->
            exportHorarium(ExportHorariumDialogFragment.withReminders(result))
        }
        childFragmentManager.setFragmentResultListener(
            OutdatedHorariumDialogFragment.REQUEST_KEY, this
        ) { _, result ->
            viewModel.shouldShowWarning = !MessageAndCheckboxDialogFragment.isChecked(result)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHorariumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        setupHorariumView(landscape)
    }

    private fun setupHorariumView(landscape: Boolean) {
        // Get a reference for the week view in the layout.
        binding.horariumView.changeOrientation(landscape)
        viewModel.horarium.observe(viewLifecycleOwner) { horarium ->
            if (horarium != null) {
                binding.horariumView.setHorarium(horarium)
            } else {
                onNoHorariumFound()
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun onNoHorariumFound() {
        view?.let {
            if (viewModel.hasPreviousHorarium()) {
                viewModel.usePreviousHorarium()
                if (viewModel.shouldShowWarning) {
                    val prevDialog = childFragmentManager.findFragmentByTag(OUTDATED_DIALOG_TAG)
                    if (prevDialog != null && (prevDialog as OutdatedHorariumDialogFragment).showsDialog) {
                        prevDialog.dismiss()
                    }
                    OutdatedHorariumDialogFragment().show(childFragmentManager, OUTDATED_DIALOG_TAG)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        actionDayViewId = getActionId(menu, actionDayViewId)
        val actionDayTitle = getToggleDayViewActionStringFromView()
        val itemDay = menu.add(Menu.NONE, actionDayViewId, 10, actionDayTitle)
        itemDay?.setIcon(getToggleDayViewActionIconResFromView())
        itemDay?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        actionToggleHorariumLanguageId = getActionId(menu, actionToggleHorariumLanguageId)
        val actionLanguageTitle = getToggleHorariumLanguageActionTitle()
        val itemLanguage =
            menu.add(Menu.NONE, actionToggleHorariumLanguageId, 11, actionLanguageTitle)
        itemLanguage?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        actionExportHorariumId = getActionId(menu, actionExportHorariumId)
        val itemExport = menu.add(
            Menu.NONE, actionExportHorariumId, 12, getString(R.string.action_export_horarium)
        )
        itemExport?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

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
            binding.horariumView.daysToShowOnToggleDayView,
            binding.horariumView.daysToShowOnToggleDayView
        )

    // Icon reflects the view the toggle will switch to (daysToShowOnToggleDayView).
    // Three buckets so the icon changes in landscape (4 <-> 8) too, not just portrait
    // (1 <-> 3): single day, a few days, or the full week.
    private fun getToggleDayViewActionIconResFromView() =
        when (binding.horariumView.daysToShowOnToggleDayView) {
            1 -> R.drawable.ic_view_day
            in 2..5 -> R.drawable.ic_view_multiple_days
            else -> R.drawable.ic_view_week
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            actionDayViewId -> {
                binding.horariumView.toggleDayView()
                item.title = getToggleDayViewActionStringFromView()
                item.setIcon(getToggleDayViewActionIconResFromView())
            }
            actionToggleHorariumLanguageId -> {
                viewModel.toggleHorariumLanguage()
                item.title = getToggleHorariumLanguageActionTitle()
            }
            actionExportHorariumId -> {
                showExportDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showExportDialog() {
        val prevDialog = childFragmentManager.findFragmentByTag(EXPORT_DIALOG_TAG)
        if (prevDialog != null && (prevDialog as ExportHorariumDialogFragment).showsDialog) {
            prevDialog.dismiss()
        }
        ExportHorariumDialogFragment().show(childFragmentManager, EXPORT_DIALOG_TAG)
    }

    private fun exportHorarium(withReminders: Boolean) {
        val context = context ?: return
        val ics = viewModel.buildIcs(
            getString(R.string.export_calendar_name, viewModel.shownYear), withReminders
        )
        if (ics == null) {
            onExportFailed()
            return
        }
        val uri = writeIcsToCache(ics)
        if (uri == null) {
            onExportFailed()
            return
        }
        // Prefer opening the file directly in a calendar app; not every device has one which can,
        // so fall back to sharing it (mail, cloud storage, …).
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, HorariumIcsExporter.MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val intent = if (viewIntent.resolveActivity(context.packageManager) != null) {
            viewIntent
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = HorariumIcsExporter.MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        try {
            startActivity(
                Intent.createChooser(intent, getString(R.string.action_export_horarium))
            )
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No app to handle the horarium export", e)
            onExportFailed()
        }
    }

    private fun writeIcsToCache(ics: String): Uri? {
        val context = context ?: return null
        return try {
            val directory = File(context.cacheDir, EXPORT_DIRECTORY)
            directory.mkdirs()
            val file = File(directory, "horarium.${HorariumIcsExporter.FILE_EXTENSION}")
            file.writeText(ics)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: IOException) {
            Log.e(TAG, "Could not write the horarium ics file", e)
            null
        }
    }

    private fun onExportFailed() {
        context?.let {
            Toast.makeText(it, R.string.export_horarium_failed, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "HorariumFragment"
        private const val EXPORT_DIALOG_TAG = "ExportHorarium"
        private const val OUTDATED_DIALOG_TAG = "NoHorarium"
        private const val EXPORT_DIRECTORY = "export"

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