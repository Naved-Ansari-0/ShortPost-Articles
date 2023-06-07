package com.example.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(!SignInSignUpUtils.isInternetAvailable(this))
            SignInSignUpUtils.noInternetToast(this)

        auth = Firebase.auth
        val user = auth.currentUser

        val intent : Intent = if(user==null){
            Intent(this, SignInScreen::class.java)
        }else{
            Intent(this, HomeScreen::class.java)
        }

        startActivity(intent)
        finishAffinity()
    }
}