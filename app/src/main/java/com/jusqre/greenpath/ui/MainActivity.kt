package com.jusqre.greenpath.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jusqre.greenpath.R
import com.jusqre.greenpath.databinding.ActivityMainBinding
import com.jusqre.greenpath.ui.main.MainViewModel
import androidx.navigation.*
import androidx.navigation.ui.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
            .apply {
                lifecycleOwner = this@MainActivity
                mainViewModel = this@MainActivity.mainViewModel
            }
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_walk, R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}