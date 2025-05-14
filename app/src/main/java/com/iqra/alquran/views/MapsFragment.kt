package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel
import java.util.*


class MapsFragment : BaseFragment(), BaseFragment.Map {
    lateinit var viewModel: MainViewModel
    lateinit var mainActivity: MainActivity
    var mapFragment: SupportMapFragment? = null

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { map ->
        map.isMyLocationEnabled = true

        val homeLatLng = LatLng(lat, long)
        val zoomLevel = 14.25f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))

        map.addCircle(CircleOptions().let {
            it.center(homeLatLng)
            it.radius(1000.0)
            it.fillColor(0x33873ED5)
            it.strokeWidth(0.0F)
        })

        viewModel.getNearByPlaces(
            "$lat, $long",
            "1000",
            "mosque",
            BuildConfig.API_KEY
        )

        viewModel.nearByPlaces.observe(viewLifecycleOwner) { places ->
            if (places.data != null) {
                mainActivity.hideDialog()

                var count = 0
                for (place in places.data.results) {
                    map.addMarker(MarkerOptions().let {
                        it.title(place.name)
                        it.position(
                            LatLng(
                                place.geometry.location.lat,
                                place.geometry.location.lng
                            )
                        )
                        it.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_mosque))
                    })
                    count++
                }
                if (count == 0) {
                    mainActivity.showSnackBar(getString(R.string.msg_no_mosque), false)
                }

            } else if (places.message != null) {
                mainActivity.hideDialog()

                val message = when (places.message) {
                    Constants.MSG_TRY_LATER -> {
                        getString(R.string.msg_try_later)
                    }
                    Constants.MSG_CONNECT_INTERNET -> {
                        getString(R.string.msg_connect_internet)
                    }
                    else -> {
                        places.message
                    }
                }

                mainActivity.showSnackBar(message, message == Constants.MSG_CONNECT_INTERNET)
            } else {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(activity!!.applicationContext, BuildConfig.API_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainActivity = activity as MainActivity
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapsFragment()
    }

    override fun updateMap() {
        mapFragment?.getMapAsync(callback)
    }
}