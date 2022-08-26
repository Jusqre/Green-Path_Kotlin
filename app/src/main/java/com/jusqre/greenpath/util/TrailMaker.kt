package com.jusqre.greenpath.util

import com.skt.Tmap.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.system.exitProcess

class TrailMaker(private val distance: Int, private val map: TMapView) {
    private lateinit var userPoint: TMapPoint
    private lateinit var currentNode: TMapPoint
    private lateinit var randomPointList: MutableList<TMapPoint>
    private var quadrant = Array<PriorityQueue<TMapPoint>>(9) { PriorityQueue() }
    private var phase = 0
    private var totalDistance = 0.0
    private val tMapData = TMapData()

    /** 산책로 생성 Job
     * - 경로 요청함수는 내부적으로 예외들을 처리하며 항상 TMapPolyLine을 반환한다.
     *    - 경로요청이 유효하지 않을때 빈 TMapPolyLine 반환 */
    fun start(): Job = CoroutineScope(Dispatchers.Default).launch {
        phase = 0
        userPoint = LocationStore.lastTmapPoint
        currentNode = userPoint
        val startQuadrant = Random().nextInt(4) * 2 + 1
        initRandomPointList(startQuadrant)

        while (phase < 7) {
            val searchIndex =
                if ((startQuadrant + phase) % 8 == 0) 8 else (startQuadrant + phase) % 8
            initQuadrant()
            try {
                var polled: TMapPoint = quadrant[searchIndex].poll() as TMapPoint
                var realPath = getRealPath(polled)
                while (realPath.linePoint.size == 0) {
                    realPath = getRealPath(polled)
                    if (realPath.linePoint.size > 0) {
                        break
                    }
                    polled = quadrant[searchIndex].poll() as TMapPoint
                }
                if (realPath.linePoint.size != 0) {
                    realPath.let {
                        val suitablePath = getSuitablePath(it, distance / (8.0))
                        totalDistance += suitablePath.distance
                        map.addMarkerItem(
                            suitablePath.hashCode().toString(),
                            marker(suitablePath.linePoint.last()).apply {
                                calloutTitle = phase.toString()
                                calloutSubTitle = totalDistance.toString()
                                canShowCallout = true
                            })
                        map.addTMapPolyLine(phase.toString(), suitablePath)
                        currentNode = suitablePath.linePoint.last()
                    }
                } else {
                    start()
                    return@launch
                }
            } catch (e: Exception) {
                start()
                return@launch
            }
            phase++
            delay(510L)
        }
        map.addTMapPolyLine(phase.toString(), getRealPath(userPoint).apply {
            totalDistance += this.distance
        })
        map.addMarkerItem("ed", TMapMarkerItem().apply {
            canShowCallout = true
            calloutTitle = "총 거리 : " + totalDistance.roundToInt() + "m"
            calloutSubTitle = "소요시간 : " + (totalDistance.roundToInt() * 60 / 5000) + "분"
            latitude = userPoint.latitude
            longitude = userPoint.longitude
        })
    }

    /** 실제 경로로부터 distanceToSearch 길이를 만족하는 polyLine 반환 */
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

    /** currentNode 부터 polled 까지의 실제 경로 반환 */
    private fun getRealPath(polled: TMapPoint): TMapPolyLine {
        return if (requestCount > 100) {
            exitProcess(0)
        } else {
            requestCount++
            tMapData.findPathDataWithType(
                TMapData.TMapPathType.PEDESTRIAN_PATH,
                currentNode,
                polled
            )
        }
    }

    /** 산책로 생성에 사용될 불특정 다수의 TMapPoint 생성
     * - 방향성을 고려하여 2개의 사분면에만 생성 */
    private fun initRandomPointList(startQuadrant: Int) {
        randomPointList = mutableListOf()
        for (i in 0..2000) {
            val tempLongitude = userPoint.longitude.randomLongitude(startQuadrant, distance)
            val tempLatitude = userPoint.latitude.randomLatitude(startQuadrant, distance)
            val tempPolyLine = TMapPolyLine().apply {
                this.addLinePoint(userPoint)
                this.addLinePoint(TMapPoint(tempLatitude, tempLongitude))
            }
            if (tempPolyLine.distance < distance / 2) {
                randomPointList.add(TMapPoint(tempLatitude, tempLongitude))
            }
        }
    }

    /** 임의의 point들을 quadrant PQ에 init */
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

    /** initial(TMapPoint)를 기준으로 target(TMapPoint)가 위치한 사분면을 반환 */
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

    /** PQ 우선순위를 expectedDistance 유사한 순서로 정렬하게 하는 Comparator */
    private val pointComparator = Comparator<TMapPoint> { o1, o2 ->
        (abs(polyLine(currentNode, o1).distance - (distance - totalDistance) / (8 - phase))
                - abs(
            polyLine(
                currentNode,
                o2
            ).distance - (distance - totalDistance) / (8 - phase)
        )).toInt()
    }

    /** 100회 이상 요청하면 시스템 종료(사용제한 방지용) */
    companion object {
        var requestCount = 0
    }
}

enum class Quadrant(val value: Int) {
    FIRST_1(1), FIRST_2(2), SECOND_1(3), SECOND_2(4),
    THIRD_1(5), THIRD_2(6), FOURTH_1(7), FOURTH_2(8)
}
