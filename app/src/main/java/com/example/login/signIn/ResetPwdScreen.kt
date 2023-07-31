package com.example.login.signIn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPwdScreen : AppCompatActivity() {

    private lateinit var emailResetPwd : TextView
    private lateinit var resetPwdButton : Button
    private lateinit var signInButton : Button
    private lateinit var skipButton : Button

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pwd_screen)

        emailResetPwd = findViewById(R.id.emailResetPwd)
        resetPwdButton = findViewById(R.id.resetPwdButton)
        signInButton = findViewById(R.id.signInButton)
        skipButton = findViewById(R.id.skipButton)

        auth = Firebase.auth

        signInButton.setOnClickListener {
            SignInSignUpUtils.navigateToSignInScreen(this, this)
        }

        skipButton.setOnClickListener {
            SignInSignUpUtils.navigateToHomeScreen(this, this)
        }

        resetPwdButton.setOnClickListener{
            val email = emailResetPwd.text.toString()

            if(!SignInSignUpUtils.checkEmail(this, email))
                return@setOnClickListener

            if(!SignInSignUpUtils.isInternetAvailable(this)){
                SignInSignUpUtils.noInternetToast(this)
                return@setOnClickListener
            }

            disablesButtons()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this){ task ->
                    if(task.isSuccessful){

                        Toast.makeText(this, "Reset password link sent to $email", Toast.LENGTH_LONG).show()
                        SignInSignUpUtils.navigateToSignInScreen(this, this)
                        enableButtons()
                    }else{

                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
                        enableButtons()
                    }
                }
                .addOnFailureListener {
                    enableButtons()
                }
        }
    }

    private fun disablesButtons(){
        resetPwdButton.isEnabled = false
        signInButton.isEnabled = false
        skipButton.isEnabled = false
    }

    private fun enableButtons(){
        resetPwdButton.isEnabled = true
        signInButton.isEnabled = true
        skipButton.isEnabled = true
    }

    override fun onBackPressed() {
        SignInSignUpUtils.navigateToSignInScreen(this, this)
    }
}