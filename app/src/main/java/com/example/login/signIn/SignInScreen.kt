package com.example.login.signIn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.login.R
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var skipButton : Button

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
        skipButton = findViewById(R.id.skipButton)

        auth = Firebase.auth

        signUpButton.setOnClickListener{
            startActivity(Intent(this, SignUpScreen::class.java))
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }

        forgotPwdButton.setOnClickListener{
            SignInSignUpUtils.navigateToResetPwdScreen(this, this)
        }

        skipButton.setOnClickListener {
            SignInSignUpUtils.navigateToHomeScreen(this, this)
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

            disablesButtons()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if(!(user!!.isEmailVerified)){
                            Toast.makeText(this,"Email not verified, check your email inbox or spam folder.", Toast.LENGTH_LONG).show()
                            val sharedPreferences : SharedPreferences = this.getSharedPreferences("shared_pref", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            val lastTime = sharedPreferences.getLong("lastTimeVerificationMailSent", -1)
                            val curTime = System.currentTimeMillis()
                            if(lastTime.toInt() != -1 && curTime - lastTime<=60000){
                                val snackbar = Snackbar.make(findViewById(android.R.id.content), "Verification link already sent less than a minute ago on your registered mail. If it is expired then try to login after one minute to sent new link.", Snackbar.LENGTH_LONG)
                                val snackbarView = snackbar.view
                                val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                                textView.maxLines = 5
                                snackbar.show()
                                FirebaseAuth.getInstance().signOut()
                                enableButtons()
                            }else{
                                user.sendEmailVerification()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful){
                                            Toast.makeText(this, "Email verification link sent to $email", Toast.LENGTH_LONG).show()
                                            FirebaseAuth.getInstance().signOut()
                                            enableButtons()
                                        }else{
                                            val errorCode = (task.exception as FirebaseAuthException).errorCode
                                            SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
//                                            Toast.makeText(this, "We have already sent email verification link, if it is expired then try to login again after sometime to send new link.", Toast.LENGTH_LONG).show()
                                            FirebaseAuth.getInstance().signOut()
                                            enableButtons()
                                        }
                                        editor.putLong("lastTimeVerificationMailSent", System.currentTimeMillis())
                                        editor.apply()
                                    }
                                    .addOnFailureListener {
                                        FirebaseAuth.getInstance().signOut()
                                        enableButtons()
                                    }
                            }
                            return@addOnCompleteListener
                        }
                        SignInSignUpUtils.navigateToHomeScreen(this,this)
                    } else {
                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
                        FirebaseAuth.getInstance().signOut()
                        enableButtons()
                    }
                }
                .addOnFailureListener {
                    FirebaseAuth.getInstance().signOut()
                    enableButtons()
                }

        }



    }

    private fun disablesButtons(){
        signUpButton.isEnabled = false
        forgotPwdButton.isEnabled = false
        signInButton.isEnabled = false
        skipButton.isEnabled = false
    }

    private fun enableButtons(){
        signUpButton.isEnabled = true
        forgotPwdButton.isEnabled = true
        signInButton.isEnabled = true
        skipButton.isEnabled = true
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}