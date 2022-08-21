package com.jusqre.greenpath.util

import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine

fun polyLine(currentNode: TMapPoint, o: TMapPoint): TMapPolyLine {
    return TMapPolyLine().apply {
        this.addLinePoint(currentNode)
        this.addLinePoint(o)
    }
}

fun marker(node: TMapPoint): TMapMarkerItem {
    return TMapMarkerItem().apply {
        latitude = node.latitude
        longitude = node.longitude
        visible = TMapMarkerItem.VISIBLE
    }
}