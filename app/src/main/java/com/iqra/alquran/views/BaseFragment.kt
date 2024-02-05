package com.iqra.alquran.views

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.iqra.alquran.utils.Constants

open class BaseFragment : Fragment() {
    var lat = 0.00
    var long = 0.00

    override fun onStart() {
        super.onStart()
            getLocation()
    }

    private fun getLocation() {
        val sharedPreferences = activity!!.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var locationGps: Location? = null
        var locationNetwork: Location? = null
        val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {
            if (hasGps) {
                if ((activity as MainActivity).checkPermission()) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 5000, 0F
                    ) { location -> locationGps = location }
                }

                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    locationGps = location
                    lat = locationGps!!.latitude
                    long = locationGps!!.longitude
                    editor.putLong(Constants.KEY_LAT, lat.toLong())
                    editor.putLong(Constants.KEY_LONG, long.toLong())
                    editor.apply()
                }
            }
            if (hasNetwork) {
                if ((activity as MainActivity).checkPermission()) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 5000, 0F
                    ) { location -> locationNetwork = location }
                }
                val location =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    locationNetwork = location
                    lat = locationNetwork!!.latitude
                    long = locationNetwork!!.longitude
                    editor.putLong(Constants.KEY_LAT, lat.toLong())
                    editor.putLong(Constants.KEY_LONG, long.toLong())
                    editor.apply()
                }
            }

            if (locationGps != null && locationNetwork != null) {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                    lat = locationGps!!.latitude
                    long = locationGps!!.longitude
                } else {
                    lat = locationNetwork!!.latitude
                    long = locationNetwork!!.longitude
                }
                editor.putLong(Constants.KEY_LAT, lat.toLong())
                editor.putLong(Constants.KEY_LONG, long.toLong())
                editor.apply()
            }
        }

        if (this is MapsFragment) {
            updateMap()
        }
        if (this is QiblaFragment) {
            updateQibla()
        }
        if (this is NamazFragment) {
            getNamazTimings()
        }
    }

    interface Map {
        fun updateMap()
    }

    interface Qibla {
        fun updateQibla()
    }

    interface Namaz {
        fun getNamazTimings()
    }
}