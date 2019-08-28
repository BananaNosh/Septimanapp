package com.nobodysapps.septimanapp.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.BuildConfig
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.PermissionListener
import com.nobodysapps.septimanapp.activity.SeptimanappActivity
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.config.Configuration
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment() {
    @Inject


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val provider = Configuration.getInstance()
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onResume() {
        super.onResume()
        val listener = object : PermissionListener {
            override fun onPermissionGranted(permission: String) {

            }

            override fun onPermissionDenied(permission: String) {
                activity?.onBackPressed()
            }

            override fun onPermissionDeniedBefore(permission: String) {
                if (view != null) {
                    Snackbar.make(
                        view!!,
                        R.string.snackbar_map_permission,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        }
        (activity as SeptimanappActivity).withPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            listener
        )
        mapView.onResume()
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
