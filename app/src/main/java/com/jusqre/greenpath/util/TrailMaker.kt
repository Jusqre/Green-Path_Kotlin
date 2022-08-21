package com.jusqre.greenpath.util

import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.math.abs
import kotlin.math.cos

class TrailMaker(private val distance: Int, private val map: TMapView) {
    private lateinit var userPoint: TMapPoint
    private lateinit var currentNode: TMapPoint
    private lateinit var randomPointList: MutableList<TMapPoint>
    private var quadrant = Array<PriorityQueue<TMapPoint>>(9) { PriorityQueue() }
    private var phase = 0
    private var totalDistance = 0.0
    private val tMapData = TMapData()

    fun start() = CoroutineScope(Dispatchers.Default).launch {
        userPoint = LocationStore.lastTmapPoint
        currentNode = userPoint
        val startQuadrant = Random().nextInt(4) * 2 + 1
        initRandomMarkerList(startQuadrant)
        while (phase < 7) {
            initQuadrant()
            val peeked =
                quadrant[if ((startQuadrant + phase) % 8 == 0) 8 else (startQuadrant + phase) % 8].peek()
            if (peeked != null) {
                checkInValidate(peeked)
            }
            if (peeked != null) {
                totalDistance += polyLine(currentNode, peeked).distance
                map.addMarkerItem(peeked.hashCode().toString(), marker(peeked).apply {
                    calloutTitle = phase.toString()
                    calloutSubTitle = totalDistance.toString()
                    canShowCallout = true
                })
                map.addTMapPolyLine(phase.toString(), polyLine(currentNode, peeked))
                currentNode = peeked
            }
            phase++
        }
        map.addTMapPolyLine(phase.toString(), polyLine(currentNode, userPoint))

    }

    private fun checkInValidate(peeked: TMapPoint) {
        try {
            val real = tMapData.findPathDataWithType(
                TMapData.TMapPathType.PEDESTRIAN_PATH,
                currentNode,
                peeked
            )
            println(real.distance)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initRandomMarkerList(startQuadrant: Int) {
        randomPointList = mutableListOf()
        for (i in 0..2000) {
            val tempLongitude = userPoint.longitude.randomLongitude(startQuadrant)
            val tempLatitude = userPoint.latitude.randomLatitude(startQuadrant)
            val tempPolyLine = TMapPolyLine().apply {
                this.addLinePoint(userPoint)
                this.addLinePoint(TMapPoint(tempLatitude, tempLongitude))
            }
            if (tempPolyLine.distance < distance / 2) {
                randomPointList.add(TMapPoint(tempLatitude, tempLongitude))
            }
        }
    }

    private fun Double.randomLongitude(startQuadrant: Int): Double {
        return this + 0.0000113 * distance / 2 * when (startQuadrant) {
            1 -> (Math.random() * 2 - 1)
            3 -> (Math.random() * -1)
            5 -> (Math.random() * 2 - 1)
            7 -> (Math.random())
            else -> throw Exception("어디선가 잘못됨")
        }
    }

    private fun Double.randomLatitude(startQuadrant: Int): Double {
        return this + 0.000009 * distance / 2 * when (startQuadrant) {
            1 -> (Math.random())
            3 -> (Math.random() * 2 - 1)
            5 -> (Math.random() * -1)
            7 -> (Math.random() * 2 - 1)
            else -> throw Exception("어디선가 잘못됨")
        }
    }

    private fun initQuadrant() {
        for (i in 1..8) {
            quadrant[i] = PriorityQueue(pointComparator)
        }
        for (point in randomPointList) {
            quadrant[finder(point, currentNode).value].add(point)
        }
        for (i in 1..8) {
            println("$i = ${quadrant[i].size}")
        }
    }

    private fun finder(target: TMapPoint, initial: TMapPoint): Quadrant {
        val radian = cos((target.latitude + initial.latitude) * Math.PI / 360)
        val cTarget = convert(target, radian)
        val cInitial = convert(initial, radian)
        return if (target.latitude - initial.latitude >= 0 && target.longitude - initial.longitude >= 0) {
            if (cInitial.latitude + cTarget.longitude - cInitial.longitude > cTarget.latitude) {
                Quadrant.FIRST_1
            } else {
                Quadrant.FIRST_2
            }
        } else if (target.latitude - initial.latitude >= 0 && target.longitude - initial.longitude <= 0) {
            if (cInitial.latitude + cInitial.longitude - cTarget.longitude < cTarget.latitude) {
                Quadrant.SECOND_1
            } else {
                Quadrant.SECOND_2
            }
        } else if (target.latitude - initial.latitude <= 0 && target.longitude - initial.longitude <= 0) {
            if (cInitial.latitude - cInitial.longitude + cTarget.longitude < cTarget.latitude) {
                Quadrant.THIRD_1
            } else {
                Quadrant.THIRD_2
            }
        } else if (target.latitude - initial.latitude <= 0 && target.longitude - initial.longitude >= 0) {
            if (cInitial.latitude - cTarget.longitude + cInitial.longitude > cTarget.latitude) {
                Quadrant.FOURTH_1
            } else {
                Quadrant.FOURTH_2
            }
        } else Quadrant.FIRST_1
    }

    private val pointComparator = Comparator<TMapPoint> { o1, o2 ->
        (abs(polyLine(currentNode, o1).distance - (distance - totalDistance) / (8 - phase))
                - abs(
            polyLine(
                currentNode,
                o2
            ).distance - (distance - totalDistance) / (8 - phase)
        )).toInt()
    }

    private fun convert(point: TMapPoint, radian: Double): TMapPoint {
        return TMapPoint(point.latitude, point.longitude * radian)
    }
}

enum class Quadrant(val value: Int) {
    FIRST_1(1), FIRST_2(2), SECOND_1(3), SECOND_2(4),
    THIRD_1(5), THIRD_2(6), FOURTH_1(7), FOURTH_2(8)
}
