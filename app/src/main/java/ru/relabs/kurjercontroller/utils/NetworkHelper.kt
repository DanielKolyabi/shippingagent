package ru.relabs.kurjercontroller.utils

import android.content.Context
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import ru.relabs.kurjercontroller.presentation.host.HostActivity
import ru.relabs.kurjercontroller.presentation.host.featureCheckers.REQUEST_LOCATION
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by ProOrange on 05.09.2018.
 */

object NetworkHelper {
    private fun isWifiEnabled(context: Context?): Boolean {
        context ?: return false
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return false
        return wifiManager.isWifiEnabled
    }

    fun isAirplaneModeEnabled(context: Context?): Boolean {
        context ?: return false
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    private fun isWifiConnected(context: Context?): Boolean {
        context ?: return false
        val wifiManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        return wifiManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected
    }

    private fun isMobileDataEnabled(context: Context?): Boolean {
        context ?: return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return Settings.Secure.getInt(context.contentResolver, "mobile_data", 0) == 1 || cm.getNetworkInfo(
            ConnectivityManager.TYPE_MOBILE
        ).isConnectedOrConnecting
    }

    fun isNetworkEnabled(context: Context?): Boolean {
        return ((isWifiEnabled(context) && isWifiConnected(
            context
        )) || isMobileDataEnabled(context)) && !isAirplaneModeEnabled(
            context
        )
    }


    fun isNetworkAvailable(context: Context?): Boolean {
        context ?: return false
        val status =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo?.isConnectedOrConnecting
        return (status ?: false) && !isAirplaneModeEnabled(
            context
        )
    }

    fun isGPSEnabled(context: Context?): Boolean{
        return (context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(GPS_PROVIDER) ?: false
    }
}