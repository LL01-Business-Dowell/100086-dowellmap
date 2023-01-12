package com.dowell.dowellmap.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dowell.dowellmap.R
import com.dowell.dowellmap.activity.MainActivityViewModel
import com.dowell.dowellmap.adapter.SearchAdapter
import com.dowell.dowellmap.data.model.CustomApiPost
import com.dowell.dowellmap.data.model.EventCreationPost
import com.dowell.dowellmap.data.model.InputSearchModel
import com.dowell.dowellmap.data.model.LocationModel
import com.dowell.dowellmap.data.network.Resource
import com.dowell.dowellmap.databinding.FragmentMapBinding
import com.dowell.dowellmap.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var waypoint: String = ""
    private var origin: String = ""
    private var customOrigin: String = ""
    private var customStartLocName: String = ""
    private lateinit var customLocation: Location
    private var query: String = ""
    private var eventId: String = ""
    private lateinit var originLocation: Location
    private lateinit var autocompleteText: String
    private var radius1: Int = 0
    private var radius2: Int = 0
    private var destination: String = ""
    private lateinit var cameraPosition: CameraPosition
    private var path: MutableList<List<LatLng>> = arrayListOf()
    private var polylines: ArrayList<Polyline> = arrayListOf()
    val mapArgs: MapFragmentArgs by navArgs()
    lateinit var searchAdapter: SearchAdapter
    lateinit var selectedPlace: LocationModel.Prediction
    private var start_address = ""
    private var customAPIAddress = ""
    private var customAPILat_Lon = ""
    private var customAPIdata = ""
    private var adresList: MutableList<String> = arrayListOf()
    private var responseList: ArrayList<CustomApiPost.Field.Response> = arrayListOf()

    @Inject
    lateinit var gson: GsonBuilder

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
        ) { currentLocation ->
            origin = coordinateToString(currentLocation.latitude, currentLocation.longitude)
            originLocation = currentLocation
            try {
                val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                val startAddress =
                    geoCoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 3)
                for (e in startAddress!!) {
                    start_address = "${e.locality}, ${e.countryName}"
                }
            }catch(e:Exception){
                toast("Weak network connection", requireContext())
                e.printStackTrace()
            }

        }

        viewModel.eventId.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                when (it) {
                    is Resource.Success -> {

                         eventId = it.value.string()
                        //Toast.makeText(requireContext(), eventId, Toast.LENGTH_LONG).show()
                        //send place request

                        sendNearbyLogData()

                        sendLogData(
                            CustomApiPost.Field(
                                reqId = viewModel.getInsertId(),
                                reqType = "nearby_places",
                                mongoId = viewModel.getInsertId(),
                                eventId = eventId,
                                dataTimeDone = viewModel.getCurrentTime(),
                                userName = viewModel.getUsername(),
                                sessionId = viewModel.getLoginId(),
                                locationDone = customOrigin.ifEmpty { origin }
                            )
                        )
                    }

                    is Resource.Failure -> {
                        toast("Background event id request failed", requireContext())
                    }

                    else -> {
                        toast("Something went wrong!", requireContext())
                    }
                }
            }
        }
        viewModel.customApiResponse.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                when (it) {
                    is Resource.Success -> {

                        if (it.value.isSuccess == true) {
                            it.value.inserted_id?.let { it1 -> viewModel.setInsertId(it1) }
                        }
                    }

                    is Resource.Failure -> {
                        toast("Background insert id request failed", requireContext())
                    }

                    else -> {
                        toast("Something went wrong!", requireContext())
                    }
                }
            }
        }
        with(binding) {
            resetBtn.setOnClickListener {
                mMap.clear()
                hideSoftInput()
                edtRadius1.text.clear()
                edtRadius2.text.clear()
                edtText.text.clear()
                if(customOrigin.isNotEmpty()){
                    customOrigin.let { latlng ->
                        MarkerOptions()
                            .position(stringToCoordinate(latlng))
                            .title(customStartLocName)
                    }.let { mapOption ->
                        val marker = mMap.addMarker(
                            mapOption
                        )
                        marker?.tag = resources.getString(R.string.start_marker)
                        marker?.showInfoWindow()
                    }

                }
            }

            autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!TextUtils.isEmpty(s)) {
                        viewModel.setQuery(s.toString())
                    }

                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }


        lifecycleScope.launch {
            viewModel.searchResults.asFlow()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { data ->
                    when (data) {
                        is Resource.Success -> {
                            if (data.value.predictions?.isNotEmpty() == true) {
                                Log.i("DataSize", data.value.predictions?.size.toString())
                                data.value.predictions?.let {
                                    displayResult(it as ArrayList<LocationModel.Prediction?>)
                                }
                            }
                        }
                        is Resource.Failure -> {
                            toast("Request Failed", requireContext())
                        }

                        is Resource.Loading->{

                        }

                    }

                }
        }


        binding.searchTypeSpinner.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            if (newItem == "Specified Location") {
                selectedLocationType(current = false)
            } else {
                selectedLocationType(current = true)
            }
        }

        binding.searchBtn.setOnClickListener {
            with(binding){
                hideSoftInput()
                try {
                    mMap.clear()
                    radius1 =
                        edtRadius1.text.toString().toInt().plus(1)
                    radius2 =
                        edtRadius2.text.toString().toInt()
                            .plus(1)
                    query = edtText.text.toString()
                    autocompleteText = binding.autoCompleteTextView.text.toString()
                }catch (e:Exception){
                    e.printStackTrace()
                }

                if (binding.searchTypeSpinner.text == "Search with") {
                    toast("Please select search type", requireContext())
                }
                else if (radius1 == 0) {
                    radius1 = 1
                    if (origin.isNotEmpty()) {
                        viewModel.setInputSearch(
                            query = query,
                            location = customOrigin.ifEmpty { origin },
                            radius = radius2.toString()
                        ).invokeOnCompletion {
                            //request 1: get event Id request
                            viewModel.userCreateEvent(
                                EventCreationPost(
                                    platformCode = "FB",
                                    cityCode = "101",
                                    dayCode = "0",
                                    dbCode = "pfm",
                                    ipAddress = viewModel.getIpAddr(),
                                    loginID = viewModel.getLoginId(),
                                    sessionID = viewModel.getLoginId(),
                                    processCode = "1",
                                    regionalTime = viewModel.getCurrentTime(),
                                    dowellTime = viewModel.getCurrentTime(),
                                    location = customOrigin.ifEmpty { origin },
                                    objectCode = "1",
                                    instanceCode = "100086",
                                    context = "afdafa",
                                    documentID = "3004",
                                    rules = "some rules",
                                    status = "work"
                                )
                            )
                        }

                    } else {
                        toast("Please enable location", requireContext())
                    }
//                    toast("Please enter the first distance", requireContext())
                }
                else if (radius2 == 0) {
                    radius2 = 1
                    if (origin.isNotEmpty()) {
                        viewModel.setInputSearch(
                            query = query,
                            location = customOrigin.ifEmpty { origin },
                            radius = radius2.toString()
                        ).invokeOnCompletion {
                            //request 1: get event Id request
                            viewModel.userCreateEvent(
                                EventCreationPost(
                                    platformCode = "FB",
                                    cityCode = "101",
                                    dayCode = "0",
                                    dbCode = "pfm",
                                    ipAddress = viewModel.getIpAddr(),
                                    loginID = viewModel.getLoginId(),
                                    sessionID = viewModel.getLoginId(),
                                    processCode = "1",
                                    regionalTime = viewModel.getCurrentTime(),
                                    dowellTime = viewModel.getCurrentTime(),
                                    location = customOrigin.ifEmpty { origin },
                                    objectCode = "1",
                                    instanceCode = "100086",
                                    context = "afdafa",
                                    documentID = "3004",
                                    rules = "some rules",
                                    status = "work"
                                )
                            )
                        }

                    } else {
                        toast("Please enable location", requireContext())
                    }
//                    toast("Please enter the second distance", requireContext())
                }
                else if (query.isEmpty()) {
                    toast("Please enter search query", requireContext())
                } else if (binding.autoCompleteTextView.isVisible and autocompleteText.isEmpty()) {
                    toast("Please enter location", requireContext())
                }else {
                    if (origin.isNotEmpty()) {
                        viewModel.setInputSearch(
                            query = query,
                            location = customOrigin.ifEmpty { origin },
                            radius = radius2.toString()
                        ).invokeOnCompletion {
                            //request 1: get event Id request
                            viewModel.userCreateEvent(
                                EventCreationPost(
                                    platformCode = "FB",
                                    cityCode = "101",
                                    dayCode = "0",
                                    dbCode = "pfm",
                                    ipAddress = viewModel.getIpAddr(),
                                    loginID = viewModel.getLoginId(),
                                    sessionID = viewModel.getLoginId(),
                                    processCode = "1",
                                    regionalTime = viewModel.getCurrentTime(),
                                    dowellTime = viewModel.getCurrentTime(),
                                    location = customOrigin.ifEmpty { origin },
                                    objectCode = "1",
                                    instanceCode = "100086",
                                    context = "afdafa",
                                    documentID = "3004",
                                    rules = "some rules",
                                    status = "work"
                                )
                            )
                        }

                    } else {
                        toast("Please enable location", requireContext())
                    }
                }
            }


        }

        return binding.root
    }

    fun selectedLocationType(current:Boolean){
        if(current){
            binding.autoCompleteTextView.visibility = View.INVISIBLE
            customOrigin = ""
            cameraPosition =
                CameraPosition.Builder().target(stringToCoordinate(customOrigin.ifEmpty { origin }))
                    .tilt(60f)
                    .zoom(14f)
                    .bearing(0f)
                    .build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        }else{
            binding.autoCompleteTextView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayResult(data: ArrayList<LocationModel.Prediction?>) {

        searchAdapter = SearchAdapter(requireContext(), data)
        binding.autoCompleteTextView.setAdapter(searchAdapter)
        searchAdapter.notifyDataSetChanged()

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

            selectedPlace = searchAdapter.getItem(position)!!
            binding.autoCompleteTextView.setText(selectedPlace.description)

            //call place detail api
            selectedPlace.place_id?.let { viewModel.getPlaceDetail(it) }

            viewModel.locationDetailResults.observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.INVISIBLE

                        hideSoftInput()

                        if (it.value.result != null) {

                            //pass object to viewModel for addition
                            // to selected location
                            viewModel.setSelectedPrediction(it.value)
                            customStartLocName = it.value.result?.name.toString()
                            customOrigin = it.value.result?.geometry?.location?.getLatLngToString()
                                .toString()

                            customLocation = it.value.result?.geometry?.location?.lat?.let { lat ->
                                it.value.result?.geometry?.location?.lng?.let { lng ->
                                    convertCoordinateToLocation(
                                        lat, lng
                                    )
                                }
                            }!!

                            //navigate to location
                            if(customOrigin.isNotEmpty()){
                                customOrigin.let { latlng ->
                                    MarkerOptions()
                                        .position(stringToCoordinate(latlng))
                                        .title(customStartLocName)
                                }.let { mapOption ->
                                    val marker = mMap.addMarker(
                                        mapOption
                                    )
                                    marker?.tag = resources.getString(R.string.start_marker)
                                    marker?.showInfoWindow()
                                }

                                cameraPosition = customOrigin.let { targetlatlng ->
                                    CameraPosition.Builder().target(stringToCoordinate(targetlatlng))
                                        .tilt(60f)
                                        .zoom(16f)
                                        .bearing(180f)
                                        .build()
                                }

                                mMap.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        cameraPosition
                                    )
                                )
                            }

                        } else {
                            toast("No result", requireContext())
                        }
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Failure -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        toast("Request Failed", requireContext())
                    }

                }

            }

        }

    }

    private fun hideSoftInput() {
        //hide input window
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0) // hide
    }


    private fun sendNearbyLogData() {

        viewModel.sendApiData(
            CustomApiPost(
                cluster = "dowellmap",
                database = "dowellmap",
                collection = "nearby_places_req",
                document = "nearby_places_req",
                teamMemberID = "1153",
                functionID = "ABCDE",
                command = "insert",
                field = CustomApiPost.Field(
                    startLocation = customOrigin.ifEmpty { origin },
                    queryText = query,
                    radiusDistanceFrom = radius1.toString(),
                    radiusDistanceTo = radius2.toString(),
                    eventId = eventId,
                    url = "None",
                    startAddress = if(customOrigin.isEmpty()) start_address else customStartLocName,
                    response = responseList,
                    is_error = viewModel.getIsError(),
                    error = viewModel.getErrorMsg()
                ),
                updateField = CustomApiPost.UpdateField(1),
                platform = "bangalore",
            )
        )
    }

    private fun sendLogData(field: CustomApiPost.Field) {

        viewModel.sendApiData(
            CustomApiPost(
                cluster = "dowellmap",
                database = "dowellmap",
                collection = "log",
                document = "log",
                teamMemberID = "1155",
                functionID = "ABCDE",
                command = "insert",
                field = field,
                updateField = CustomApiPost.UpdateField(1),
                platform = "bangalore",
            )
        )
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
            origin = customOrigin.ifEmpty { origin },
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

    fun convertCoordinateToLocation(lat: Double, lng: Double): Location {
        val point = Location(LocationManager.NETWORK_PROVIDER)
        point.latitude = lat
        point.longitude = lng
        return point
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.setOnMarkerClickListener(this)
        mMap.isMyLocationEnabled = true
        viewModel.directionResponse.observe(this) {
            if (view != null) {
                    when (it) {
                        is Resource.Success -> {
                            //Draw routing path
                            it.value.routes?.get(0)?.legs?.forEach { legs ->
                                path.clear()
                                val step = legs.steps
                                for (i in 0 until step?.size!!) {
                                    val points = step[i].polyline?.points
                                    path.add(PolyUtil.decode(points))
                                }

                               addPathToMap(path)
                            }

                            cameraPosition =
                                CameraPosition.Builder().target(stringToCoordinate(customOrigin.ifEmpty { origin }))
                                    .tilt(60f)
                                    .zoom(16f)
                                    .bearing(180f)
                                    .build()

                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                            binding.progressBar.visibility = View.INVISIBLE
                        }
                        is Resource.Failure -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            toast("Routing Failed", requireContext())
                        }
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }


            }
        }

        viewModel.textResponse.observe(this) {
            if (view != null) {
                lifecycleScope.launch {
                    when (it) {
                        is Resource.Success -> {
                            viewModel.setIsError(false)
                            it.value.status?.let { it1 -> viewModel.setErrorMsg("None") }

                            if(customOrigin.isNotEmpty()){
                                customOrigin.let { latlng ->
                                    MarkerOptions()
                                        .position(stringToCoordinate(latlng))
                                        .title(customStartLocName)
                                }.let { mapOption ->
                                    val marker = mMap.addMarker(
                                        mapOption
                                    )
                                    marker?.tag = resources.getString(R.string.start_marker)
                                    marker?.showInfoWindow()
                                }

                                cameraPosition = customOrigin.let { targetlatlng ->
                                    CameraPosition.Builder().target(stringToCoordinate(targetlatlng))
                                        .tilt(60f)
                                        .zoom(16f)
                                        .bearing(180f)
                                        .build()
                                }

                                mMap.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        cameraPosition
                                    )
                                )
                            }

                            //draw circumference 1
                            val center = stringToCoordinate(customOrigin.ifEmpty { origin })
                            val circleOptions1 = CircleOptions()
                            circleOptions1.center(center)
                            circleOptions1.radius(radius1.toDouble())
                            circleOptions1.fillColor(Color.parseColor("#6DFFFFFF"))
                            circleOptions1.strokeColor(Color.parseColor("#005734"))
                            circleOptions1.strokeWidth(4f)
                            mMap.addCircle(circleOptions1)

                            //draw circumference 2
                            val circleOptions2 = CircleOptions()
                            circleOptions2.center(center)
                            circleOptions2.radius(radius2.toDouble())
                            circleOptions2.fillColor(Color.parseColor("#6DFFFFFF"))
                            circleOptions2.strokeColor(Color.RED)
                            circleOptions2.strokeWidth(6f)
                            mMap.addCircle(circleOptions2)

                            computeDistance(
                                if (customOrigin.isEmpty()) originLocation else customLocation,
                                it.value
                            ).results?.filter { (it.radius!! in radius1..radius2) or (it.radius!! in radius2..radius2)
                            }.also { results ->
                                if (results != null) {
                                    if (results.isEmpty()) {
                                        toast(
                                            "There is no $query within $radius1  and $radius2 meters",
                                            requireContext()
                                        )

                                    } else {
                                        for (e in results){
                                            customAPIAddress = e.name.toString()
                                            customAPILat_Lon =
                                                e.geometry?.location?.getLatLngToString()
                                                    .toString()
                                            customAPIdata = e.toString()
                                        }



                                        results.forEach {

                                            responseList.add(
                                                CustomApiPost.Field.Response(
                                                    address = it.formatted_address,
                                                    lat_lon = it.geometry?.location?.getLatLngToString(),
                                                    data = CustomApiPost.Data(
                                                        business_status = it.business_status,
                                                        price_level = it.price_level,
                                                        rating = it.rating,
                                                        reference = it.reference,
                                                        user_ratings_total = it.user_ratings_total
                                                    )
                                                )
                                            )

                                            it.geometry?.location?.getLatLng()?.let { latlng ->
                                                MarkerOptions()
                                                    .position(latlng)
                                                    .title(it.name)
                                            }?.let { mapOption ->
                                                val marker = mMap.addMarker(
                                                    mapOption
                                                )
                                                marker?.tag = mapOption.position
                                                marker?.showInfoWindow()
                                            }
                                        }


                                    }
                                }
                            }


                            binding.progressBar.visibility = View.INVISIBLE
                        }
                        is Resource.Failure -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            toast("Search Failed", requireContext())
                            viewModel.setIsError(true)
                            it.errorBody?.let { it1 -> viewModel.setErrorMsg(it1) }
                        }
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }

                }

            }
        }
    }

    private fun addPathToMap(path: MutableList<List<LatLng>>) {
        for (i in 0 until path.size) {
            polylines.add(mMap.addPolyline(
                PolylineOptions().addAll(path[i])
                    .color(
                        Color.RED
                    )
                    .width(4f)

            ))
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

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.tag!=resources.getString(R.string.start_marker)){
            val position = marker.tag as LatLng

            binding.progressBar.visibility = View.VISIBLE

            if(polylines.isNotEmpty()){
                for (polyline in polylines) {
                    polyline.remove()
                }
                polylines.clear()
            }

            viewModel.setDirectionQuery(
                origin = customOrigin.ifEmpty { origin },
                destination = coordinateToString(position.latitude, position.longitude)
            )
        }

        return false
    }

}