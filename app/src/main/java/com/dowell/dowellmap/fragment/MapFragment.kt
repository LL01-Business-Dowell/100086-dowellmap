package com.dowell.dowellmap.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dowell.dowellmap.R
import com.dowell.dowellmap.activity.MainActivityViewModel
import com.dowell.dowellmap.data.network.Resource
import com.dowell.dowellmap.databinding.FragmentMapBinding
import com.dowell.dowellmap.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var waypoint: String = ""
    private lateinit var origin: String
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

        binding.searchBtn.setOnClickListener {
            viewModel.currentLocationCord.observe(
                viewLifecycleOwner, Observer { currentLoccation ->

                    viewModel.setInputSearch(
                        query = binding.edtText.text.toString(),
                        location = coordinateToString(currentLoccation.latitude, currentLoccation.longitude),
                        radius = binding.edtRadius.text.toString())
                }
            )

            Log.d("AY:::", binding.edtText.text.toString())
        }

        getRoute()
        return binding.root
    }

    fun searchButtonFunction() {

    }

    fun getRoute() {

        val destinationObject = mapArgs.placeModel?.last()
        destination = destinationObject?.result?.geometry?.location?.getLatLngToString().toString()

        Log.i("PlaceModelSize", mapArgs.placeModel?.size.toString())

        waypoint = mapArgs.placeModel?.map { it.result?.geometry?.location?.getLatLngToString() }
            ?.joinToString("|").toString()

        Log.i("Waypoint", waypoint)

        /*viewModel.currentLocationCord.observe(requireActivity()) {
            if (view != null) {
                lifecycleScope.launch {
                    if (it != null) {
                        origin = coordinateToString(it.latitude, it.longitude)
                        //viewModel.currentLocationCord.removeObserver()
                        Log.i("Current location", origin)
                    } else {
                        toast("Location Detection failed", requireContext())
                    }
                }
            }
        }*/

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

        viewModel.directionResponse.observe(this) {
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
        }


        viewModel.textResponse.observe(this) {
            if (view != null) {
                lifecycleScope.launch {
                    when (it) {
                        is Resource.Success -> {
                            //set origin marker and camera property
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(stringToCoordinate(origin))
                                    .title("Start Location")

                            )?.showInfoWindow()

                            //set waypoint(s) maker including the destination
                            it.value.results?.forEach {
                                it.geometry?.location?.getLatLng()?.let { latlng ->

                                    MarkerOptions()
                                        .position(latlng)
                                        .title(it.formatted_address)


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
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.cancel()
    }

}