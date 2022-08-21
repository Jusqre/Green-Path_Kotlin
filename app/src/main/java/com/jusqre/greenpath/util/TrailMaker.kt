package com.jusqre.greenpath.util

import com.skt.Tmap.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

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
            println(phase)
            initQuadrant()
            var polled =
                quadrant[if ((startQuadrant + phase) % 8 == 0) 8 else (startQuadrant + phase) % 8].poll()
            var realPath : TMapPolyLine? = null
            while (polled != null && realPath == null) {
                realPath = getRealPath(polled)
                if (realPath != null) {
                    break
                }
                polled = quadrant[if ((startQuadrant + phase) % 8 == 0) 8 else (startQuadrant + phase) % 8].poll()
            }
            if (polled != null) {
                realPath?.let {
                    val suitablePath = getSuitablePath(it, distance / (8.0))
                    totalDistance += suitablePath.distance
                    map.addMarkerItem(suitablePath.hashCode().toString(), marker(suitablePath.linePoint.last()).apply {
                        calloutTitle = phase.toString()
                        calloutSubTitle = totalDistance.toString()
                        canShowCallout = true
                    })
                    map.addTMapPolyLine(phase.toString(), suitablePath)
                    currentNode = suitablePath.linePoint.last()
                }
            }
            phase++
            delay(510L)
        }
        map.addTMapPolyLine(phase.toString(), getRealPath(userPoint).apply {
            totalDistance += this?.distance ?: 0.0
        })
        map.addMarkerItem("ed", TMapMarkerItem().apply {
            canShowCallout = true
            calloutTitle = "총 거리 : " + totalDistance.roundToInt()+ "m"
            calloutSubTitle = "소요시간 : " + (totalDistance.roundToInt() * 60 / 5000) + "분"
            latitude = userPoint.latitude
            longitude = userPoint.longitude
        })
    }

    private fun getSuitablePath(realPath: TMapPolyLine, distanceToSearch: Double): TMapPolyLine {
        val tempPolyLine = TMapPolyLine()
        val pointList = realPath.linePoint
        var left = 0
        var right = pointList.size - 1
        while (left < right) {
            val mid = (left + right) / 2
            tempPolyLine.linePoint.clear()
            pointList.subList(0, mid).forEach {
                tempPolyLine.addLinePoint(it)
            }
            if (tempPolyLine.distance > distanceToSearch) {
                right = mid - 1
            } else {
                left = mid + 1
            }
        }
        return tempPolyLine
    }

    private fun getRealPath(polled: TMapPoint): TMapPolyLine? {
        println("get에 들어왔습니다.")
        return try {
            tMapData.findPathDataWithType(
                TMapData.TMapPathType.PEDESTRIAN_PATH,
                currentNode,
                polled
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
