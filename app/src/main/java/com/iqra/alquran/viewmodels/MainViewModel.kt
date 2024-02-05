package com.iqra.alquran.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqra.alquran.network.models.*
import com.iqra.alquran.network.wrapper.Resource
import com.iqra.alquran.repository.Repository
import kotlinx.coroutines.async

class MainViewModel() : ViewModel() {

    val repository = Repository()
    val namazTimings: MutableLiveData<Resource<NamazTimings>> = MutableLiveData()
    val forexRates: MutableLiveData<Resource<ForexRates>> = MutableLiveData()
    val nearByPlaces: MutableLiveData<Resource<NearByPlaces>> = MutableLiveData()
    val hijriTime: MutableLiveData<Resource<HijriTime>> = MutableLiveData()
    val asmaAlHusna: MutableLiveData<Resource<AsmaAlHusna>> = MutableLiveData()

    fun getNamazTimings(timestamp: String, latitude: String, longitude: String) =
        viewModelScope.async {
            namazTimings.postValue(Resource.Loading())
            namazTimings.postValue(repository.getNamazTimings(timestamp, latitude, longitude))
        }

    fun getForexRates(currency: String) = viewModelScope.async {
        forexRates.postValue(Resource.Loading())
        forexRates.postValue(repository.getForexRates(currency))
    }

    fun getNearByPlaces(location: String, radius: String, type: String, key: String) =
        viewModelScope.async {
            nearByPlaces.postValue(Resource.Loading())
            nearByPlaces.postValue(repository.getNearByPlaces(location, radius, type, key))
        }

    fun getHijriTime(date: String)=
        viewModelScope.async {
            hijriTime.postValue(Resource.Loading())
            hijriTime.postValue(repository.getHijriTime(date))
        }

    fun getAsmaAlHusna()=
        viewModelScope.async {
            asmaAlHusna.postValue(Resource.Loading())
            asmaAlHusna.postValue(repository.getAsmaAlHusna())
        }
}