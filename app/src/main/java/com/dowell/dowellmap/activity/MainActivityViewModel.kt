package com.dowell.dowellmap.activity

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.dowell.dowellmap.data.*
import com.dowell.dowellmap.data.model.*
import com.dowell.dowellmap.data.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userDatastore: UserDatastore
) : ViewModel() {

    private val _query = MutableLiveData<String>()
    private val _placeId= MutableLiveData<String>()
    private val _autogenUsername = MutableLiveData<String>()
    private val _currentTime = MutableLiveData<String>()
    private val _ip = MutableLiveData<String>()
    private val _loginId = MutableLiveData<String>()
    private val _insertId = MutableLiveData<String>()
    private val _isError = MutableLiveData<Boolean>()
    private val _errorMsg = MutableLiveData<String>()

    private val _currentLocationCord = MutableLiveData<Location>()
    val currentLocationCord : LiveData<Location> get() = _currentLocationCord



    private val _selectedPredictions = ArrayList<PlaceDetail>()
    val selectedPredictions : ArrayList<PlaceDetail> get() = _selectedPredictions

    private val _directionResponse = MutableLiveData<Resource<DirectionResponse>>()
    val directionResponse : LiveData<Resource<DirectionResponse>> get() = _directionResponse

    private val _textResponse = MutableLiveData<Resource<InputSearchModel>>()
    val textResponse : LiveData<Resource<InputSearchModel>> get() = _textResponse

    private val _geocodeResponse = MutableLiveData<Resource<GeocodeModel>>()
    val geocodeResponse : LiveData<Resource<GeocodeModel>> get() = _geocodeResponse

    private val _qrId = MutableLiveData<Resource<UserLogResponse>>()
    val qrId : LiveData<Resource<UserLogResponse>> get() = _qrId

    private val _eventId = MutableLiveData<Resource<ResponseBody>>()
    val eventId : LiveData<Resource<ResponseBody>> get() = _eventId

    private val _customApiResponse = MutableLiveData<Resource<CustomApiResponse>>()
    val customApiResponse : LiveData<Resource<CustomApiResponse>> get() = _customApiResponse

    fun getLocationChange(location: Location){
        _currentLocationCord.value=location
    }

    fun setQuery(query: String){
        _query.value = query
    }

    fun setUsername(name: String){
        _autogenUsername.value = name
    }

    fun getUsername(): String? {
       return _autogenUsername.value
    }

    fun setLoginId(qrId: String){
        _loginId.value = qrId
    }

    fun getLoginId(): String? {
        return _loginId.value
    }

    fun setCurrentTime(time: String){
        _currentTime.value = time
    }

    fun getCurrentTime(): String? {
        return _currentTime.value
    }

    fun setIpAddr(ip: String){
        _ip.value = ip
    }

    fun getIpAddr(): String? {
        return _ip.value
    }


    fun setInsertId(id: String){
        _insertId.value = id
    }

    fun getInsertId(): String? {
        return _insertId.value
    }

    fun setIsError(id: Boolean){
        _isError.value = id
    }

    fun getIsError(): Boolean? {
        return _isError.value
    }

    fun setErrorMsg(msg: String){
        _errorMsg.value = msg
    }

    fun getErrorMsg(): String? {
        return _errorMsg.value
    }


    fun getPlaceDetail(id: String) {
        _placeId.value = id
    }

    fun getfirstLocationData(): Location? {
        return _currentLocationCord.value
    }

    val searchResults: LiveData<Resource<LocationModel>>
            = _query.switchMap { query ->
        liveData {
            emit(
                searchRepository.getSearchPrediction(query)
            )
        }
    }

    val locationDetailResults: LiveData<Resource<PlaceDetail>> = _placeId.switchMap { id ->
        liveData {
            emit(
                searchRepository.getLocationDetail(id)
            )
        }
    }


    fun setDirectionQuery(origin: String, destination:String, waypoints:String?=null) {
        viewModelScope.launch {
            _directionResponse.value = searchRepository.getDirection(origin,destination, waypoints)
        }
    }

    fun getGeoCodeInfo(address:String) {
        viewModelScope.launch {
            _geocodeResponse.value = searchRepository.getGeocodeDetail(address)
        }
    }

    fun setInputSearch(query: String, location: String, radius: String) = viewModelScope.launch {
            _textResponse.value = Resource.Loading
            _textResponse.value = searchRepository.getTextSearch(query,
                location, radius.toInt())
        }

    fun setSelectedPrediction(predictions: PlaceDetail){
        if(!_selectedPredictions.contains(predictions)){
            _selectedPredictions.add(predictions)
        }
        Log.i("ItemSize", _selectedPredictions.size.toString())
        Log.i("Prediction", predictions.toString())
    }

    fun removeSelectedPrediction(pos:Int,prediction: PlaceDetail){
        if(_selectedPredictions.contains(prediction)){
            _selectedPredictions.removeAt(pos)
        }
    }

    fun logUser(logPost: LogPost)=viewModelScope.launch {
            _qrId.value=searchRepository.makeLog(logPost)
        }

    fun userCreateEvent(eventCreationPost: EventCreationPost)=viewModelScope.launch {
            _eventId.value=searchRepository.makeEventCreation(eventCreationPost)
        }

    fun sendApiData(customApiPost: CustomApiPost)=viewModelScope.launch {
            _customApiResponse.value=searchRepository.sendData(customApiPost)
        }

    suspend fun setQrId(qrId:String){
        userDatastore.setQrId(qrId)
    }

}