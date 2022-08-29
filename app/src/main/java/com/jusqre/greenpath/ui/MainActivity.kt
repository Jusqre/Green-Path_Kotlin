package com.jusqre.greenpath.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.*
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jusqre.greenpath.BuildConfig
import com.jusqre.greenpath.R
import com.jusqre.greenpath.databinding.ActivityMainBinding
import com.jusqre.greenpath.ui.main.MainViewModel
import com.jusqre.greenpath.util.LocationStore
import com.jusqre.greenpath.util.TrailStore
import com.skt.Tmap.*


@SuppressLint("UseCompatLoadingForDrawables")
class MainActivity : AppCompatActivity(), TMapGpsManager.onLocationChangedCallback {
    private lateinit var binding: ActivityMainBinding
    private val _map = MutableLiveData<TMapView>()
    val map: LiveData<TMapView>
        get() = _map
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var navView: BottomNavigationView
    private lateinit var currentPosition: Location
    private lateinit var userMarker: Bitmap

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userMarker = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.navigation
            ), 50, 50, true
        )
        _map.value = TMapView(this).apply {
            this.setSKTMapApiKey(BuildConfig.API_KEY)
        }
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
            .apply {
                lifecycleOwner = this@MainActivity
                mainViewModel = this@MainActivity.mainViewModel
            }
        navView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_walk, R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (!TrailStore.isTrailListInitialized()) {
            TrailStore.initDBTrailInfo()
        }

        map.value?.let {
            setUpGPS()
        }
        LocationStore.lastLocation.observe(this) {
            onLocationUpdated(it)
        }
        initMap()
    }

    private fun initMap() {
        findViewById<LinearLayout>(R.id.map).addView(map.value)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpGPS() {
        val gps = TMapGpsManager(this).apply {
            minTime = 100
            minDistance = 5f
            provider = TMapGpsManager.GPS_PROVIDER
            setLocationCallback()
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            ) //위치권한 탐색 허용 관련 내용
        }
        gps.OpenGps()
        map.value?.moveCamera(gps.location.longitude, gps.location.latitude)
    }

    override fun onLocationChange(location: Location) {
        LocationStore.updateLocation(location)
    }

    private fun onLocationUpdated(location: Location) {
        if (!::currentPosition.isInitialized) {
            currentPosition = location
            println("not initialized")
        } else {
            currentPosition = location
        }
        map.value?.let {
            it.moveCamera(location.longitude, location.latitude)
            it.setUserMarker(location)
        }
    }

    private fun TMapView.setUserMarker(location: Location) {
        this.addMarkerItem("currentPosition", TMapMarkerItem().apply {
            icon = userMarker.rotate(location.bearing)
            longitude = location.longitude
            latitude = location.latitude
        })
    }

    private fun TMapView.moveCamera(longitude: Double, latitude: Double) {
        this.setLocationPoint(longitude, latitude)
        this.setCenterPoint(longitude, latitude)
    }

    private fun Bitmap.rotate(bearing: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(bearing)
        }
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }
}