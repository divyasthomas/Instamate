package com.example.instamate

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.instamate.databinding.ActivityHomeBinding



class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_home)

        navView.setOnItemSelectedListener { menuItem ->
            val destinationId = menuItem.itemId

            // Navigate to the selected destination
            navController.navigate(destinationId)

            Log.d("My-Tag HomeActivity", "Bottom NavBar clicked. Navigating to ${menuItem.title}")

            // Return true to indicate the item click is handled
            true
        }
    }

}