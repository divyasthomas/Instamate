package com.example.instamate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instamate.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.createBtn.setOnClickListener {
            Log.d("My-Tag LoginActivity"," clicked createBtn..going to signup activity")
            startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
//            finish()
        }


        binding.loginBtn.setOnClickListener {
            val email = binding.email.editText?.text.toString()
            val password = binding.password.editText?.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
                    if (task.isSuccessful){
                        Log.d("My-Tag LoginActivity"," clicked loginBtn..logged in ...going to MainActivity")
                        Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }

                    else{
                        Toast.makeText(this@LoginActivity, "Failed to login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }


    }
}