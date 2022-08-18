package com.jusqre.greenpath.ui.walk

import androidx.lifecycle.ViewModel
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapView

class WalkViewModel : ViewModel() {

    fun startMakeTrail(map: TMapView) {
        map.addMarkerItem("asdf", TMapMarkerItem().apply {
            this.latitude = map.latitude
            this.longitude = map.longitude
        })
    }

}