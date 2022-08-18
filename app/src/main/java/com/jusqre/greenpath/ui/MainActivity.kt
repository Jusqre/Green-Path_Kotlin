package com.jusqre.greenpath.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jusqre.greenpath.BuildConfig
import com.jusqre.greenpath.R
import com.jusqre.greenpath.databinding.ActivityMainBinding
import com.jusqre.greenpath.ui.main.MainViewModel
import com.skt.Tmap.TMapGpsManager
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapView


class MainActivity : AppCompatActivity(), TMapGpsManager.onLocationChangedCallback {
    private lateinit var binding: ActivityMainBinding
    private val _map = MutableLiveData<TMapView>()
    val map: LiveData<TMapView>
        get() = _map
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var navView: BottomNavigationView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


        map.value?.let {
            initMap()
            setUpGPS()
        }
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
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
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
        map.value?.setLocationPoint(gps.location.longitude,gps.location.latitude)
        map.value?.setCenterPoint(gps.location.longitude,gps.location.latitude)
    }

    override fun onLocationChange(location: Location?) {
        location?.let {
            println(location)
            println(map.value)
            map.value?.setLocationPoint(it.longitude, it.latitude)
            map.value?.setCenterPoint(it.longitude, it.latitude)
            map.value?.addMarkerItem("currentLocation", TMapMarkerItem().apply {
                this.latitude = it.latitude
                this.longitude = it.longitude
            })
        }
    }
}