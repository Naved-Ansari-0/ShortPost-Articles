package com.example.login.home.profile

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.example.login.R
import com.example.login.signIn.SignInSignUpUtils
import com.google.firebase.auth.FirebaseAuth

class SettingScreen : AppCompatActivity() {

    private lateinit var backButton : ImageButton
    private lateinit var about : TextView
    private lateinit var logout : TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_screen)

        backButton = findViewById(R.id.backButton)
        about = findViewById(R.id.about)
        logout = findViewById(R.id.logout)

        backButton.setOnClickListener{
            finish()
        }

        logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            SignInSignUpUtils.navigateToSignInScreen(this,this)
        }

    }

}