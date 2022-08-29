package com.jusqre.greenpath.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

object TrailStore {
    lateinit var trailList: MutableList<TMapMarkerItem>
    fun isTrailListInitialized() = ::trailList.isInitialized

    /** FireBase TrailInfo 초기화 */
    fun initDBTrailInfo() {
        val tempList = mutableListOf<TMapMarkerItem>()
        val db = FirebaseFirestore.getInstance()
        db.collection("PARK")
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    Objects.requireNonNull(task.result).forEach {
                        val tempMarker = TMapMarkerItem().apply {
                            tMapPoint = TMapPoint(
                                it.getDouble("latitude") ?: 0.0,
                                it.getDouble("longitude") ?: 0.0
                            )
                            canShowCallout = true
                            calloutTitle = it.get("name") as String? ?: "dummy"
                            val length = sqrt(it.getDouble("area") ?: 0.0 * 4 * 3.14).roundToInt()
                            calloutSubTitle = "길이 : ${length}m/소요 시간 : ${(length * 60 / 5000)}분"
                        }
                        tempMarker.id = tempMarker.hashCode().toString()
                        tempList.add(tempMarker)
                    }
                }
                if (task.isComplete) {
                    trailList = tempList
                }
            }
    }

    fun showTrailMarker(map: TMapView, requiredDistance: Int) {
        for (trail in trailList) {
            val tol = TMapPolyLine()
            tol.addLinePoint(LocationStore.lastTmapPoint)
            tol.addLinePoint(
                trail.tMapPoint
            )
            if (tol.distance <= requiredDistance) {
                try {
                    map.getMarkerItemFromID(trail.id).visible = TMapMarkerItem.VISIBLE
                    map.setCenterPoint(map.longitude, map.latitude)
                } catch (_: Exception) {
                    map.addMarkerItem(
                        "${trail.hashCode()}",
                        trail.apply {
                            visible = TMapMarkerItem.VISIBLE
                        }
                    )
                    map.setCenterPoint(map.longitude, map.latitude)
                }
            }
        }
    }

    fun hideTrailMarker(map: TMapView){
        for (trail in trailList) {
            trail.visible = TMapMarkerItem.HIDDEN
        }
        map.setCenterPoint(map.longitude, map.latitude)
    }
}