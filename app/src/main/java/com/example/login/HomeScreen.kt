package com.example.login

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeScreen : AppCompatActivity() {

    private lateinit var details: TextView
    private lateinit var logoutButton : Button

    private lateinit var auth : FirebaseAuth

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        details = findViewById(R.id.details)
        logoutButton = findViewById(R.id.logoutButton)

        auth = Firebase.auth
        val user = auth.currentUser

        if(user!=null){
            val name = user.displayName.toString()
            val email = user.email.toString()
            val id = user.uid.toString()
            val verification = user.isEmailVerified

            details.text = name + "\n" +
                            email + "\n" +
                            id + "\n" +
                            verification

        }

        logoutButton.setOnClickListener{
            auth.signOut()
            SignInSignUpUtils.navigateToSignInScreen(this, this)
        }

    }
}