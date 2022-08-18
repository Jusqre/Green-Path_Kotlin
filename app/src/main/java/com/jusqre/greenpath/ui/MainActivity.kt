package com.jusqre.greenpath.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jusqre.greenpath.R
import com.jusqre.greenpath.databinding.ActivityMainBinding
import com.jusqre.greenpath.ui.main.MainViewModel
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.jusqre.greenpath.BuildConfig
import com.skt.Tmap.TMapView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val _map = MutableLiveData<TMapView>()
    private val map: LiveData<TMapView>
        get() = _map
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var navView: BottomNavigationView

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
        initMap()
    }

    private fun initMap() {
        findViewById<LinearLayout>(R.id.map).addView(map.value)
    }
}