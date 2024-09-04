package com.example.instamate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instamate.Models.User
import com.example.instamate.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.instamate.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.UUID


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    var profileImageUUID = ""
    val storage = Storage()

    private  val launcher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        uri?.let{
            val uuid = UUID.randomUUID().toString()
            storage.uploadGalleryImage(uri,uuid){imageUrl ->
                //successful upload
                if (imageUrl!=null){
                    binding.profileImage.setImageURI(uri)
                    profileImageUUID = imageUrl
                }
                else{
                    Log.d("My-Tag SignUpActivity - launcher ", "failed to upload picture $imageUrl ")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        Log.d("My-Tag SignUpActivity ", "user is ${auth.currentUser}")

        //go to login
        binding.login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
        }


        binding.profileImage.setOnClickListener {
            launcher.launch("image/*")

        }

        binding.registerBtn.setOnClickListener {
            createUserAccount()
        }
    }


    private fun createUserAccount() {
        val name = binding.name.editText?.text.toString()
        val email = binding.email.editText?.text.toString()
        val password = binding.password.editText?.text.toString()
        val bio = binding.bio.editText?.text.toString()
        val normalisedName = name.lowercase(Locale.getDefault())

        if (name.isNullOrBlank() || email.isNullOrBlank() || password.isNullOrBlank()) {
            Toast.makeText(
                this@SignUpActivity,
                "Please fill all required fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //if not empty
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val authuser = auth.currentUser

                val newUser = User(
                    userid = authuser!!.uid,
                    name = name,
                    normalisedName =  normalisedName,
                    profileImage = profileImageUUID,
                    email = email,
                    password = password,
                    bio = bio,
                    followers = 0,
                    following = 0,
                    followingIds = ArrayList()
                )

                firestore.collection(Utils.USER_FOLDER).document(authuser.uid).set(newUser)
                startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
//                finish()


            } else {
                Toast.makeText(
                    this@SignUpActivity,
                    "Failed to create new user. Error: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("My-Tag SignUpActivity", " Failed to create new user. current useris ${auth.currentUser} error is ${task.exception?.message} "
                )
            }
        }
    }
}




