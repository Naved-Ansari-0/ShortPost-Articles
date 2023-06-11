package com.example.login.signIn

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.login.HomeScreen
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInScreen : AppCompatActivity() {

    private lateinit var emailSignIn : TextView
    private lateinit var passwordSignIn : TextView
    private lateinit var signInButton : Button
    private lateinit var forgotPwdButton : Button
    private lateinit var signUpButton : Button

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_screen)

        emailSignIn = findViewById(R.id.emailSignIn)
        passwordSignIn = findViewById(R.id.passwordSignIn)
        signInButton = findViewById(R.id.signInButton)
        forgotPwdButton = findViewById(R.id.forgotPwdButton)
        signUpButton = findViewById(R.id.signUpButton)

        auth = Firebase.auth

        signUpButton.setOnClickListener{
            startActivity(Intent(this, SignUpScreen::class.java))
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }

        forgotPwdButton.setOnClickListener{
            startActivity(Intent(this, ResetPwdScreen::class.java))
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }


        signInButton.setOnClickListener{

            val email = emailSignIn.text.toString().trim()
            val password = passwordSignIn.text.toString().trim()

            if( !SignInSignUpUtils.checkEmail(this, email) ||
                !SignInSignUpUtils.checkPassword(this, password)
            )
                return@setOnClickListener

            if(!SignInSignUpUtils.isInternetAvailable(this)){
                SignInSignUpUtils.noInternetToast(this)
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser

                        if(!(user!!.isEmailVerified)){
                            Toast.makeText(this,"Email not verified, check your email inbox or spam folder.", Toast.LENGTH_LONG).show()

                            user.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        Toast.makeText(this, "Email verification link sent to $email", Toast.LENGTH_LONG).show()
                                        auth.signOut()
                                    }else{
                                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                                        SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
                                    }
                                }
                            return@addOnCompleteListener
                        }

                        startActivity(Intent(this, HomeScreen::class.java))
                        overridePendingTransition(
                            R.anim.slide_in_from_right,
                            R.anim.slide_out_to_left
                        )
                        finishAffinity()

                    } else {

                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        SignInSignUpUtils.firebaseExceptionToast(this, errorCode)

                    }
                }

        }



    }

    override fun onBackPressed() {
        finishAffinity()
    }
}