package com.jusqre.greenpath.util

import com.skt.Tmap.TMapCircle
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

fun convert(point: TMapPoint, radian: Double): TMapPoint {
    return TMapPoint(point.latitude, point.longitude * radian)
}

fun Double.randomLongitude(startQuadrant: Int, distance: Int): Double {
    return this + 0.0000113 * distance / 2 * when (startQuadrant) {
        1 -> (Math.random() * 2 - 1)
        3 -> (Math.random() * -1)
        5 -> (Math.random() * 2 - 1)
        7 -> (Math.random())
        else -> throw Exception("어디선가 잘못됨")
    }
}

fun Double.randomLatitude(startQuadrant: Int, distance: Int): Double {
    return this + 0.000009 * distance / 2 * when (startQuadrant) {
        1 -> (Math.random())
        3 -> (Math.random() * 2 - 1)
        5 -> (Math.random() * -1)
        7 -> (Math.random() * 2 - 1)
        else -> throw Exception("어디선가 잘못됨")
    }
}