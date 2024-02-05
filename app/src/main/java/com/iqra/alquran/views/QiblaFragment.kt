package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.iqra.alquran.utils.Compass
import com.iqra.alquran.utils.Compass.CompassListener
import com.iqra.alquran.utils.GPSTracker
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants

class QiblaFragment : BaseFragment(), BaseFragment.Qibla {

    private val TAG = "QiblaFinder"
    private var compass: Compass? = null
    private var arrowViewQiblat: ImageView? = null
    private var imageDial: ImageView? = null
    private var currentAzimuth = 0f
    var gps: GPSTracker? = null
    private var QiblaDegree = 0f
    var mapFragment: SupportMapFragment? = null

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { map ->
        val homeLatLng = LatLng(lat, long)
        val kaabaLatLng = LatLng(21.422507, 39.826209)
        val inBetweenLatLng = LatLng(
            (homeLatLng.latitude + kaabaLatLng.latitude) / 2,
            (homeLatLng.longitude + kaabaLatLng.longitude) / 2
        )
        val zoomLevel = 2f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(inBetweenLatLng, zoomLevel))
        map.addMarker(MarkerOptions().let {
            it.title("You")
            it.position(homeLatLng)
            it.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_user))
        })
        map.addMarker(MarkerOptions().let {
            it.title("Kaaba")
            it.position(kaabaLatLng)
            it.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_mecca))
        })

        val lineOptions = PolylineOptions()
        lineOptions.add(homeLatLng)
        lineOptions.add(kaabaLatLng)
        lineOptions.pattern(listOf(Gap(20F), Dash(30F), Gap(20F)))

        map.addPolyline(lineOptions)
        map.setOnMapClickListener { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(activity!!.applicationContext, Constants.API_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qibla, container, false)
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        gps = GPSTracker(activity)
        arrowViewQiblat = view.findViewById(R.id.main_image_qibla)
        imageDial = view.findViewById(R.id.main_image_dial)

        arrowViewQiblat!!.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }

    override fun onStart() {
        super.onStart()
        compass?.start()
    }

    override fun onPause() {
        super.onPause()
        compass?.stop()
    }

    override fun onResume() {
        super.onResume()
        compass?.start()
    }

    override fun onStop() {
        super.onStop()
        compass?.stop()
    }

    private fun setupCompass() {
        getBearing()
        compass = Compass(activity)
        val cl = CompassListener { azimuth ->
            adjustGambarDial(azimuth)
            adjustArrowQiblat(azimuth)
        }
        compass!!.setListener(cl)
    }

    fun adjustGambarDial(azimuth: Float) {
        val animation: Animation = RotateAnimation(
            -currentAzimuth,
            -azimuth,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        animation.duration = 500
        animation.repeatCount = 0
        animation.fillAfter = true
        imageDial!!.startAnimation(animation)
    }

    fun adjustArrowQiblat(azimuth: Float) {
        val qiblaDegree = QiblaDegree
        val animation: Animation = RotateAnimation(
            -currentAzimuth + qiblaDegree,
            -azimuth,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        animation.duration = 500
        animation.repeatCount = 0
        animation.fillAfter = true
        arrowViewQiblat!!.startAnimation(animation)
        if (qiblaDegree > 0) {
            arrowViewQiblat!!.visibility = View.VISIBLE
        } else {
            arrowViewQiblat!!.visibility = View.INVISIBLE
            arrowViewQiblat!!.visibility = View.GONE
        }
    }

    @SuppressLint("MissingPermission")
    fun getBearing() {
        fetchGPS()
    }

    fun fetchGPS() {
        var result = 0.0
        gps = GPSTracker(activity)
        if (gps!!.canGetLocation()) {
            Log.e("TAG", "GPS is on")
            val lat_saya = gps!!.latitude
            val lon_saya = gps!!.longitude
            if (lat_saya < 0.001 && lon_saya < 0.001) {
                arrowViewQiblat!!.visibility = View.GONE
                (activity as MainActivity).showSnackBar(getString(R.string.msg_check_gps), false)
            } else {
                val longitude2 = 39.826209
                val latitude2 = Math.toRadians(21.422507)
                val latitude1 = Math.toRadians(lat_saya)
                val longDiff = Math.toRadians(longitude2 - lon_saya)
                val y = Math.sin(longDiff) * Math.cos(latitude2)
                val x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) *
                        Math.cos(latitude2) * Math.cos(longDiff)
                result = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
                val result2 = result.toFloat()
                QiblaDegree = result2
                arrowViewQiblat!!.visibility = View.VISIBLE
            }
        } else {
            gps!!.showSettingsAlert()
            arrowViewQiblat!!.visibility = View.GONE
            (activity as MainActivity).showSnackBar(getString(R.string.msg_check_gps), false)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = QiblaFragment()
    }

    override fun updateQibla() {
        mapFragment?.getMapAsync(callback)
        setupCompass()
    }
}