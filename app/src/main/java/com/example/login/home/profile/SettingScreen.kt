package com.example.login.home.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.login.R
import com.example.login.signIn.SignInSignUpUtils
import com.google.firebase.auth.FirebaseAuth

class SettingScreen : AppCompatActivity() {

    private lateinit var backButton : ImageButton
    private lateinit var privacyPolicyLinkButton : TextView
    private lateinit var logout : TextView
    private lateinit var logoutLinearLayout: LinearLayout
    private lateinit var website: Button

    private var auth: FirebaseAuth?=null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_screen)

        backButton = findViewById(R.id.backButton)
        privacyPolicyLinkButton = findViewById(R.id.privacyPolicyLinkButton)
        logout = findViewById(R.id.logout)
        logoutLinearLayout = findViewById(R.id.logoutLinearLayout)
        website = findViewById(R.id.website)

        auth = FirebaseAuth.getInstance()
        if(auth?.currentUser==null)
            logoutLinearLayout.visibility = View.INVISIBLE

        backButton.setOnClickListener{
            finish()
        }

        privacyPolicyLinkButton.setOnClickListener {
            SignInSignUpUtils.visitPrivacyPolicyLink(this)
        }

        website.setOnClickListener {
            val url = "https://www.navedansari.in"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            SignInSignUpUtils.navigateToSignInScreen(this,this)
        }

    }

}