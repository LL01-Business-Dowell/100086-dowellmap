package com.dowell.dowellmap.activity

import android.location.Location
import androidx.lifecycle.*
import com.dowell.dowellmap.data.LocationModel
import com.dowell.dowellmap.data.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    searchRepository: SearchRepository
) : ViewModel() {

    private val _currentLocationCord = MutableLiveData<Location>()
    val currentLocationCord : LiveData<Location> get() = _currentLocationCord


    private val _predictions = MutableLiveData<String>()
    //val predictions : Flow<LocationModel> get() = _predictions

    fun getLocationChange(location: Location){
        _currentLocationCord.value=location
    }

    fun setQuery(query: String) {
        _predictions.value = query
    }

    val results: LiveData<LocationModel>
            = _predictions.switchMap { query ->
        liveData {
            emit(
                searchRepository.getSearchPrediction(query) as LocationModel
            )
        }
    }

}