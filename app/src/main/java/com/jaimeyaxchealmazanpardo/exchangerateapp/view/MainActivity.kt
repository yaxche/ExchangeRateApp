package com.jaimeyaxchealmazanpardo.exchangerateapp.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.ui.setupActionBarWithNavController
import com.jaimeyaxchealmazanpardo.exchangerateapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navHost : NavHost
    private lateinit var toolbar : Toolbar
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        setSupportActionBar(toolbar)
        navHost = supportFragmentManager.findFragmentById(R.id.fragment) as NavHost
        navController = navHost.navController
        setupActionBarWithNavController(navController)
    }

    private fun initView(){
        toolbar = findViewById(R.id.toolbar)
    }
}