package com.dowell.dowellmap

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        //it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

@SuppressLint("MissingPermission")
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val n = cm.activeNetwork
        if (n != null) {
            val nc = cm.getNetworkCapabilities(n)
            //It will check for both wifi and cellular network
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI)
        }
        return false
    } else {
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}


@SuppressLint("MissingPermission")
fun networkState(context: Context):String {
    var connection =""
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state == NetworkInfo.State.CONNECTED)
    {
        connection =  "mobile network"

    } else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED) {
        connection =  "wifi"
    }

    return connection
}

object LogDeviceInfo {
    fun generateUserName(len: Int): String {
        val chars = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                + "lmnopqrstuvwxyz!@#$%&")
        val rnd = Random()
        val sb = StringBuilder(len)
        for (i in 0 until len) sb.append(chars[rnd.nextInt(chars.length)])
        return sb.toString()
    }

    fun ipAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    fun getCityName(lat: Double, long: Double,context: Context):String{
        var cityName = ""
        var countryName =""
        try {

            val geoCoder = Geocoder(context, Locale.getDefault())
            val address = geoCoder.getFromLocation(lat, long, 1)
            cityName = address?.get(0)?.locality ?: ""
            countryName = address?.get(0)?.countryName ?: ""

        }catch (e:Exception){
            e.printStackTrace()
        }
        return "$cityName, $countryName"
    }
}

fun toast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun visible(view: View, visible:Boolean){
    if(visible){
        view.visibility= View.VISIBLE
    }else{
        view.visibility= View.INVISIBLE
    }
}


