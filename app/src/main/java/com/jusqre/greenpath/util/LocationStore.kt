package com.jusqre.greenpath.util

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LocationStore {
    val locationUpdateException = MutableLiveData<Exception>()

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location>
        get() = _lastLocation

    fun updateLocation(location: Location) {
        _lastLocation.postValue(location)
    }
}
