package com.dowell.dowellmap.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.viewbinding.BuildConfig
import com.dowell.dowellmap.data.model.Login
import com.dowell.dowellmap.databinding.ActivitySplashScreenBinding
import com.dowell.dowellmap.startNewActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    lateinit var sharedPreference: SharedPreferences
    var connection: String? = null
    var fullLocation: String? = null

    /*For real time Location*/
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
         sharedPreference =  getSharedPreferences("Login_ID", Context.MODE_PRIVATE)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        networkState()
        with(binding){

            try {
                val packageInfo = applicationContext.packageManager.getPackageInfo(
                    packageName, 0
                )
                val version = "Version: " + packageInfo.versionName
                txtVersion.text = version
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            fetchLocation()
        }
    }



    private fun loginUser(fullLocation: String) {
        val mobileIp = ipAddress()
        val sdf = SimpleDateFormat("dd-M-yyyy")
        val currentDate = sdf.format(Date())
        val apiService = LoginViewModel()
        val userInfo = Login(
            userName = "user-"+Generator.generateApiCode(10),
            os = "android",
            device = "mobile",
            browser = "dowell map mobile",
            location = fullLocation,
            time = currentDate,
            connection = connection,
            ip = mobileIp,
            qrid = null
        )


        apiService.loginUser(userInfo) {
            if (it?.qrid != null) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()

                var editor = sharedPreference.edit()
                editor.putString("qrid", it.qrid)
                editor.apply()

                Handler(Looper.getMainLooper()).postDelayed({
                    startNewActivity(MainActivity::class.java)
                    try {
                        this@SplashActivity.finishAffinity()
                    } catch (e: NullPointerException) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace()
                        }
                    }
                }, 1500)


            } else {
                Toast.makeText(this, "Error logging in user", Toast.LENGTH_LONG).show()
            }
        }
    }

    object Generator {
        fun generateApiCode(len: Int): String {
            val chars = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                    + "lmnopqrstuvwxyz!@#$%&")
            val rnd = Random()
            val sb = StringBuilder(len)
            for (i in 0 until len) sb.append(chars[rnd.nextInt(chars.length)])
            return sb.toString()
        }
    }

    fun networkState() {

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
       if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state == NetworkInfo.State.CONNECTED)
            {
                connection = "mobile network"

            } else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED) {
           connection = "wifi"
       } else {
           Toast.makeText(this, "Not connected", Toast.LENGTH_LONG).show()

       }

//        Toast.makeText(this, connection, Toast.LENGTH_LONG).show()

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

    private fun fetchLocation() {

        val task = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                getCityName(it.latitude, it.longitude)
            }
        }
    }

    private fun getCityName(lat: Double, long: Double){
        var cityName: String
        var countryName: String
        val geoCoder = Geocoder(this, Locale.getDefault())
        val Address = geoCoder.getFromLocation(lat, long, 3)

        for (e in Address){
            cityName = e.locality
            countryName = e.countryName
            fullLocation = "$cityName, $countryName"
        }
        loginUser(fullLocation!!)
    }
}