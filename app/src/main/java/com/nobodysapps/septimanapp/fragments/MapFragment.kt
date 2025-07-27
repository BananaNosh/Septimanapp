package com.nobodysapps.septimanapp.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nobodysapps.septimanapp.BuildConfig
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.PermissionListener
import com.nobodysapps.septimanapp.activity.SeptimanappActivity
import com.nobodysapps.septimanapp.databinding.FragmentMapBinding
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import dagger.android.support.AndroidSupportInjection
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment() {

    @Inject
    lateinit var locationStorage: LocationStorage

    @Inject
    lateinit var eventInfoStorage: EventInfoStorage

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val provider = Configuration.getInstance()
        provider.userAgentValue = BuildConfig.APPLICATION_ID
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapController = binding.mapView.controller
        mapController.setZoom(17.0)
        binding.mapView.setMultiTouchControls(true)

        //Attribution
        val attribution = CopyrightOverlay(context).apply {
            setAlignRight(true)
        }
        binding.mapView.overlays.add(attribution)

        addLocationOverlays()
    }

    private fun addLocationOverlays() {
        val locations = locationStorage.loadLocations(eventInfoStorage.loadSeptimanaLocation())
        val mainLocation = locations?.firstOrNull { it.isMain }
        mainLocation?.let { location ->
            binding.mapView.controller.animateTo(location.coordinates)
        }
        val markers = locations?.map { location ->
            Marker(binding.mapView).apply {
                position = location.coordinates
                title = location.title
                subDescription = location.description
            }
        }
        markers?.forEach { marker ->
            binding.mapView.overlays.add(marker)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment HorariumFragment.
         */
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}