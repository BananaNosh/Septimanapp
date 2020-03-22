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
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val provider = Configuration.getInstance()
        provider.userAgentValue = BuildConfig.APPLICATION_ID
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapController = mapView.controller
        mapController.setZoom(18.0)
        mapController.animateTo(GeoPoint(50.79700, 8.92270))
        mapView.setMultiTouchControls(true)

        //Attribution
        val attribution = CopyrightOverlay(context).apply {
            setAlignRight(true)
        }
        mapView.overlays.add(attribution)

        addLocationOverlays()
    }

    private fun addLocationOverlays() {
        //your items
        val locations = locationStorage.loadLocations("amoeneburg")
        val markers = locations?.map {
            Marker(mapView).apply {
                position = it.coordinates
                title = it.title
                subDescription = it.description
            }
        }
        markers?.forEach {
            mapView.overlays.add(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is SeptimanappActivity) {
            throw RuntimeException("$context must inherit from SeptimanappActivity")
        }
        context.getSeptimanappApplication().component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        val listener = object : PermissionListener {
            override fun onPermissionGranted(permission: String) {
                mapView.onResume()
            }

            override fun onPermissionDenied(permission: String) {
                activity?.onBackPressed()
            }
        }
        (activity as SeptimanappActivity).withPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            listener,
            getString(R.string.explication_map_permission)
        )
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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
