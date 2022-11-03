package com.dowell.dowellmap.fragment

import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.InputQueue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dowell.dowellmap.R
import com.dowell.dowellmap.activity.MainActivityViewModel
import com.dowell.dowellmap.data.model.InputSearchModel
import com.dowell.dowellmap.data.network.Resource
import com.dowell.dowellmap.databinding.FragmentMapBinding
import com.dowell.dowellmap.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var waypoint: String = ""
    private lateinit var origin: String
    private lateinit var query: String
    private lateinit var originLocation: Location
    private var radius: Int = 0
    private var destination: String = ""

    val mapArgs: MapFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        //Initialize map support fragment
        mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.currentLocationCord.observe(
            viewLifecycleOwner
        ) { currentLoccation ->
            origin = coordinateToString(currentLoccation.latitude, currentLoccation.longitude)
            originLocation = currentLoccation
        }

        binding.searchBtn.setOnClickListener {
            if (origin.isNotEmpty()) {
                mMap.clear()

                viewModel.setInputSearch(
                    query = binding.edtText.text.toString(),
                    location = origin,
                    radius = binding.edtRadius.text.toString()
                )

                radius = binding.edtRadius.text.toString().toInt()
                query = binding.edtText.text.toString()
            }

        }

        //getRoute()
        return binding.root
    }


    fun getRoute() {

        val destinationObject = mapArgs.placeModel?.last()
        destination = destinationObject?.result?.geometry?.location?.getLatLngToString().toString()

        Log.i("PlaceModelSize", mapArgs.placeModel?.size.toString())

        waypoint = mapArgs.placeModel?.map { it.result?.geometry?.location?.getLatLngToString() }
            ?.joinToString("|").toString()

        Log.i("Waypoint", waypoint)

        origin = viewModel.getfirstLocationData()?.latitude?.let {
            viewModel.getfirstLocationData()?.longitude?.let { it1 ->
                coordinateToString(
                    it,
                    it1
                )
            }
        }.toString()

        viewModel.setDirectionQuery(
            origin = origin,
            destination = destination,
            waypoints = waypoint
        )


    }


    fun coordinateToString(lat: Double, lngDouble: Double): String {
        return lat.toString().plus(",").plus(lngDouble)
    }

    fun stringToCoordinate(latlng: String): LatLng {
        val lat = latlng.split(",")[0].toDouble()
        val lng = latlng.split(",")[1].toDouble()
        return LatLng(lat, lng)
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.isMyLocationEnabled = true

        /* viewModel.directionResponse.observe(this) {
             if (view != null) {
                 lifecycleScope.launch {
                     when (it) {
                         is Resource.Success -> {
                             //Draw routing path
                             val path: MutableList<List<LatLng>> = ArrayList()
                             it.value.routes?.get(0)?.legs?.forEach { it ->
                                 val step = it.steps
                                 for (i in 0 until step?.size!!) {
                                     val points = step[i].polyline?.points
                                     path.add(PolyUtil.decode(points))
                                 }

                                 for (i in 0 until path.size) {
                                     mMap.addPolyline(
                                         PolylineOptions().addAll(path[i])
                                             .color(Color.parseColor("#005734"))
                                     )
                                 }
                             }


                             //set origin marker and camera property
                             mMap.addMarker(
                                 MarkerOptions()
                                     .position(stringToCoordinate(origin))
                                     .title("Start Location")

                             )?.showInfoWindow()

                             //set waypoint(s) maker including the destination
                             mapArgs.placeModel?.forEach {
                                 it.result?.geometry?.location?.getLatLng()?.let { latlng ->

                                     MarkerOptions()
                                         .position(latlng)
                                         .title(it?.result?.formatted_address)


                                 }?.let { mapOption ->
                                     mMap.addMarker(
                                         mapOption
                                     )?.showInfoWindow()
                                 }

                             }

                             val cameraPosition =
                                 CameraPosition.Builder()
                                     .target(stringToCoordinate(origin))
                                     .tilt(60f)
                                     .zoom(8f)
                                     .bearing(0f)
                                     .build()

                             mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                         }
                         is Resource.Failure -> {
                             toast("Routing Failed", requireContext())
                         }
                         is Resource.Loading -> {
                             toast("Routing...", requireContext())
                         }
                     }

                 }

             }
         }*/


        viewModel.textResponse.observe(this) {
            if (view != null) {
                lifecycleScope.launch {
                    when (it) {
                        is Resource.Success -> {

                            //draw circumference
                            val center = stringToCoordinate(origin)

                            val circleOptions = CircleOptions()
                            circleOptions.center(center)
                            circleOptions.radius(radius.toDouble())
                            circleOptions.fillColor(Color.parseColor("#6DFFFFFF"))
                            circleOptions.strokeColor(Color.parseColor("#005734"))
                            circleOptions.strokeWidth(2f)

                            mMap.addCircle(circleOptions)

                            val selectedLocation=computeDistance(
                                originLocation,
                                it.value
                            ).results?.filter { it.radius!! <= radius }.also { results ->
                                if (results != null) {
                                    if(results.isEmpty()) {
                                        toast("There is no $query within $radius meters",
                                            requireContext())
                                    }else{
                                        results.forEach {
                                            it.geometry?.location?.getLatLng()?.let { latlng ->

                                                MarkerOptions()
                                                    .position(latlng)
                                                    .title(it.name)

                                            }?.let { mapOption ->
                                                mMap.addMarker(
                                                    mapOption
                                                )?.showInfoWindow()
                                            }
                                        }

                                    }
                                }
                            }


                            val cameraPosition =
                                CameraPosition.Builder()
                                    .target(stringToCoordinate(origin))
                                    .tilt(60f)
                                    .zoom(12f)
                                    .bearing(0f)
                                    .build()

                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                            binding.progressBar.visibility = View.INVISIBLE

                        }
                        is Resource.Failure -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            toast("Search Failed", requireContext())
                        }
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            toast("Routing...", requireContext())
                        }
                    }

                }

            }
        }
    }

    private fun computeDistance(
        currentLocationCord: Location,
        inputSearchModel: InputSearchModel
    ): InputSearchModel {

        inputSearchModel.results?.forEach { data ->
            val point2 = Location(LocationManager.NETWORK_PROVIDER)
            point2.latitude = data.geometry?.location?.lat!!
            point2.longitude = data.geometry?.location?.lng!!
            data.radius = currentLocationCord.distanceTo(point2).toInt()

        }

        return inputSearchModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.cancel()
    }

}