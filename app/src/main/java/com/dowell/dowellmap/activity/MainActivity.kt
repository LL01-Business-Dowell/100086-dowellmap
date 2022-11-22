package com.dowell.dowellmap.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.dowell.dowellmap.*
import com.dowell.dowellmap.LogDeviceInfo.getCityName
import com.dowell.dowellmap.LogDeviceInfo.ipAddress
import com.dowell.dowellmap.R
import com.dowell.dowellmap.data.UserDatastore
import com.dowell.dowellmap.data.model.LogPost
import com.dowell.dowellmap.data.network.Resource
import com.dowell.dowellmap.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel by viewModels<MainActivityViewModel>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var locationPermissionGranted = false

    private val REQUEST_CHECK_SETTINGS = 1
    private lateinit var locationRequest: LocationRequest
    private var currentLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    @Inject
    lateinit var userDatastore: UserDatastore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setting up nav controller and appBar
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        //get location permission
        getLocationPermission()

        //initialized Location service client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!locationPermissionGranted) {
            return
        } else {
            checkConnection()
            //location callback to register request
            buildLocationCallback()
            createLocationRequest()
            settingsCheck()
        }


        with(viewModel){
            setUsername("user-" + LogDeviceInfo.generateUserName(10))
            setCurrentTime(DateFormat.getDateInstance().format(Date()))
            ipAddress()?.let { setIpAddr(it) }

        }


        val city = currentLocation?.latitude?.let { coordinateToString(it,
            currentLocation?.latitude!!
        ) }



        //asynchronous user log sent
        viewModel.logUser(
            LogPost(
                Username = viewModel.getUsername(),
                OS = "android",
                Device = "mobile",
                Browser = "dowell map mobile",
                Location = "null",
                Time = viewModel.getCurrentTime(),
                Connection = networkState(this),
                IP = viewModel.getIpAddr()
            )
        )


        //temp store qrId in datastore
        viewModel.qrId.observe(this) {
            lifecycleScope.launch {

                when (it) {
                    is Resource.Success -> {
                        //Log.i("LogResponse", it.value.qtoString())
                        it.value.qrid.let { it1 ->
                            if (it1 != null) {
                                viewModel.setLoginId(it1)
                            }
                        }
                        toast("Successfully log user",this@MainActivity)
                    }

                    is Resource.Failure -> {
                        toast("Background data request failed", this@MainActivity)
                    }

                    else -> {
                        toast("Something went wrong!", this@MainActivity)
                    }
                }
            }

        }


    }

    fun coordinateToString(lat: Double, lngDouble: Double): String {
        return lat.toString().plus(",").plus(lngDouble)
    }

    fun checkConnection() {
        if (!isOnline(this)) {
            visible(binding.errorMsg, true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

        if (currentLocation == null) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    // Check for location settings
    private fun settingsCheck() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(
            this
        ) {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            Log.d("TAG", "onSuccess: settingsCheck")
            getCurrentLocation()
        }
        task.addOnFailureListener(this) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Log.d("TAG", "onFailure: settingsCheck")
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val resolvable = e
                    resolvable.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(
                this
            ) { location ->
                Log.d("TAG", "onSuccess: getLastLocation")
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    currentLocation = location
                    Log.d("TAG", "onSuccess:latitude " + location.latitude)
                    Log.d("TAG", "onSuccess:longitude " + location.longitude)

                    viewModel.getLocationChange(location)

                } else {
                    Log.d("TAG", "location is null")
                    buildLocationCallback()
                }
            }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Update UI with location data
                    currentLocation = location
                    Log.d("TAG", "onLocationResult: " + currentLocation!!.latitude)
                    viewModel.getLocationChange(location)
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode
            == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        ) { // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        if (locationPermissionGranted) {
            getCurrentLocation()
        }
    }
}