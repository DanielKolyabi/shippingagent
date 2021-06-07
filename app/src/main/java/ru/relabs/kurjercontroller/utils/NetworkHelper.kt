package ru.relabs.kurjercontroller.utils

import android.content.Context
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import java.lang.reflect.Method


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

    private fun isMobileDataEnabledReflect(context: Context?): Boolean {
        context ?: return false

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // Make the method callable
            // get the setting for "mobile data"
            method.invoke(cm) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    private fun isMobileDataEnabled(context: Context?): Boolean {
        context ?: return false

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return Settings.Secure.getInt(context.contentResolver, "mobile_data", 0) == 1 ||
                cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting ||
                isMobileDataEnabledReflect(context)
    }

    fun isNetworkEnabled(context: Context?): Boolean {
        return ((isWifiEnabled(context) && isWifiConnected(context)) || isMobileDataEnabled(context))
                && !isAirplaneModeEnabled(context)
    }


    fun isNetworkAvailable(context: Context?): Boolean {
        context ?: return false
        val status =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo?.isConnectedOrConnecting
        return (status ?: false) && !isAirplaneModeEnabled(
            context
        )
    }

    fun isGPSEnabled(context: Context?): Boolean {
        return (context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(GPS_PROVIDER) ?: false
    }
}