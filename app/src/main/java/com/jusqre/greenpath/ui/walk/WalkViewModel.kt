package com.jusqre.greenpath.ui.walk

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import com.jusqre.greenpath.util.TrailMaker
import com.jusqre.greenpath.util.TrailStore
import com.skt.Tmap.TMapView

class WalkViewModel : ViewModel() {
    private lateinit var lookUpAlertDialog: AlertDialog.Builder
    private lateinit var makeTrailDialog: AlertDialog.Builder

    @SuppressLint("StaticFieldLeak")
    private lateinit var lookUpEditText: EditText

    @SuppressLint("StaticFieldLeak")
    private lateinit var makeTrailEditText: EditText

    fun startMakeTrail(map: TMapView, context: Context) {
        if (!::makeTrailDialog.isInitialized) {
            initMakeTrailAlertDialog(map, context)
        }
        if (makeTrailEditText.parent != null) {
            (makeTrailEditText.parent as ViewGroup).removeView(makeTrailEditText)
        }
        makeTrailDialog.create().show()
    }

    private fun initMakeTrailAlertDialog(map: TMapView, context: Context) {
        makeTrailEditText = EditText(context)
        makeTrailEditText.setText("")
        makeTrailDialog = AlertDialog.Builder(context).apply {
            setTitle("산책로 생성")
            setMessage("원하는 산책로 길이를 입력하십시오.(m)")
            setView(makeTrailEditText)
            setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                try {
                    TrailMaker(makeTrailEditText.text.toString().toInt(), map).start()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this.context, "길이를 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
            setNeutralButton(
                "RESET"
            ) { _: DialogInterface?, _: Int ->
                map.removeAllTMapPolyLine()
                TrailMaker.resultMarkerID.forEach { map.removeMarkerItem(it) }
                makeTrailEditText.setText("")
            }
        }
    }

    /** 산책로 조회 수행 */
    fun startLookUpTrail(map: TMapView, context: Context) {
        if (!TrailStore.isTrailListInitialized()) {
            return Toast.makeText(context,"산책로 정보를 DB에서 받아오는 중입니다.",Toast.LENGTH_SHORT).show()
        }
        if (!::lookUpAlertDialog.isInitialized) {
            initLookUpAlertDialog(map, context)
        }
        if (lookUpEditText.parent != null) {
            (lookUpEditText.parent as ViewGroup).removeView(lookUpEditText)
        }

        lookUpAlertDialog.create().show()
    }

    /** AlertDialog 초기화 */
    private fun initLookUpAlertDialog(map: TMapView, context: Context) {
        lookUpEditText = EditText(context)
        lookUpEditText.setText("")
        lookUpAlertDialog = AlertDialog.Builder(context).apply {
            setTitle("산책로 조회")
            setMessage("산책로 조회 범위를 입력하십시오.(m)")
            setView(lookUpEditText)
            setPositiveButton("OK"){ _: DialogInterface?, _: Int ->
                TrailStore.showTrailMarker(map, lookUpEditText.text.toString().toInt())
            }
            setNeutralButton(
                "RESET"
            ) { _: DialogInterface?, _: Int ->
                TrailStore.hideTrailMarker(map)
                lookUpEditText.setText("")
            }
        }
    }
}