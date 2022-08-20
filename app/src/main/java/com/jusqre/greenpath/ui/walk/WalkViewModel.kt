package com.jusqre.greenpath.ui.walk

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jusqre.greenpath.util.LocationStore
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

class WalkViewModel : ViewModel() {
    private lateinit var dbTrailList: MutableList<TMapMarkerItem>
    private lateinit var lookUpAlertDialog: AlertDialog.Builder
    @SuppressLint("StaticFieldLeak")
    private lateinit var rangeEditText: EditText

    fun startMakeTrail(map: TMapView) {
        map.addMarkerItem("asdf", TMapMarkerItem().apply {
            this.latitude = map.latitude
            this.longitude = map.longitude
        })
    }

    /** 산책로 조회 수행 */
    fun startLookUpTrail(map: TMapView, context: Context) {
        if (!::dbTrailList.isInitialized) {
            initDBTrailInfo()
        }
        if (!::lookUpAlertDialog.isInitialized) {
            initLookUpAlertDialog(map, context)
        }
        if (rangeEditText.parent != null) {
            (rangeEditText.parent as ViewGroup).removeView(rangeEditText)
        }

        lookUpAlertDialog.create().show()
    }

    /** AlertDialog 초기화 */
    private fun initLookUpAlertDialog(map: TMapView, context: Context) {
        rangeEditText = EditText(context)
        rangeEditText.setText("")
        lookUpAlertDialog = AlertDialog.Builder(context).apply {
            setTitle("산책로 추천")
            setMessage("산책로 추천 범위를 입력하십시오.(m)")
            setView(rangeEditText)
            setPositiveButton("OK"){ _: DialogInterface?, _: Int ->
                for (trail in dbTrailList) {
                    val tol = TMapPolyLine()
                    tol.addLinePoint(LocationStore.lastTmapPoint)
                    tol.addLinePoint(
                        trail.tMapPoint
                    )
                    if (tol.distance <= rangeEditText.text.toString().toInt()) {
                        map.addMarkerItem(
                            "${trail.hashCode()}",
                            trail.apply {
                                visible = TMapMarkerItem.VISIBLE
                            }
                        )
                    }
                }
            }
            setNeutralButton(
                "RESET"
            ) { _: DialogInterface?, _: Int ->
                for (i in dbTrailList) {
                    i.visible = TMapMarkerItem.HIDDEN
                    map.setCenterPoint(map.longitude, map.latitude)
                }
                rangeEditText.setText("")
            }
        }
    }

    /** FireBase TrailInfo 초기화 */
    private fun initDBTrailInfo() {
        dbTrailList = mutableListOf()
        val db = FirebaseFirestore.getInstance()

        db.collection("tgreentest")
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    Objects.requireNonNull(task.result).forEach {
                        dbTrailList.add(TMapMarkerItem().apply {
                            tMapPoint = TMapPoint(
                                it.getDouble("relat") ?: 0.0,
                                it.getDouble("relng") ?: 0.0
                            )
                            name = it.data["name"] as String? ?: "dummy"
                            canShowCallout = true
                            calloutTitle = name
                            val length = sqrt(it.getDouble("area") ?: 0.0 * 4 * 3.14).roundToInt()
                            calloutSubTitle = "길이 : ${length}m/소요 시간 : ${(length * 60 / 5000)}분"
                        })
                    }
                }
            }
    }

}