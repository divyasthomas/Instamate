package com.example.instamate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("My-Tag MainActiviyty ", "in mainactivity")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({

            if (auth.currentUser!=null){
                Log.d("My-Tag MainActiviyty ", "user is ${auth.currentUser}")
                startActivity(Intent(this, HomeActivity::class.java))
            }
            else{
                Log.d("My-Tag MainActiviyty ", " going to signup...user is ${auth.currentUser}")

                startActivity(Intent(this, SignUpActivity::class.java))
            }

            },2000)

    }

}