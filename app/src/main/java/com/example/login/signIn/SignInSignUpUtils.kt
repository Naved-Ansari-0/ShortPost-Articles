package com.example.login.signIn

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.login.HomeScreen
import com.example.login.R

object SignInSignUpUtils {

    fun checkName(context: Context, name:String):Boolean{
        if(name=="") {
            Toast.makeText(context, "Name can't be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        val namePattern = Regex("^[a-zA-Z ]+\$")
        if(!namePattern.matches(name)){
            Toast.makeText(context,"Name can only contain letters and spaces", Toast.LENGTH_SHORT).show()
            return false
        }
        if(name.length>25){
            Toast.makeText(context,"Name too long", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkEmail(context: Context, email:String):Boolean{
        if(email=="") {
            Toast.makeText(context, "Email can't be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        val emailPattern = Regex("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        if(!emailPattern.matches(email)) {
            Toast.makeText(context, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkPassword(context: Context, password:String):Boolean{
        if(password=="") {
            Toast.makeText(context, "Password can't be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        val passwordPattern = Regex(("^" +
//                "(?=.*[0-9])" +         //at least 1 digit
//                "(?=.*[a-z])" +         //at least 1 lower case letter
//                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z0-9])" +      //any letter
//                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$"))
        if(!passwordPattern.matches(password)){
            Toast.makeText(context, "Password must contain at least 8 characters without any white space", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    fun firebaseExceptionToast(context: Context, errorCode : String){

        when (errorCode) {
            "ERROR_INVALID_CUSTOM_TOKEN" -> Toast.makeText(context, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show()
            "ERROR_CUSTOM_TOKEN_MISMATCH" -> Toast.makeText(context, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show()
            "ERROR_INVALID_CREDENTIAL" -> Toast.makeText(context, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show()
            "ERROR_INVALID_EMAIL" -> Toast.makeText(context, "The email address is badly formatted.", Toast.LENGTH_LONG).show()
            "ERROR_WRONG_PASSWORD" -> Toast.makeText(context, "Wrong password", Toast.LENGTH_LONG).show()
            "ERROR_USER_MISMATCH" -> Toast.makeText(context, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show()
            "ERROR_REQUIRES_RECENT_LOGIN" -> Toast.makeText(context, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show()
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> Toast.makeText(context, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show()
            "ERROR_EMAIL_ALREADY_IN_USE" -> Toast.makeText(context, "The email address is already in use by another account.", Toast.LENGTH_LONG).show()
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> Toast.makeText(context, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show()
            "ERROR_USER_DISABLED" -> Toast.makeText(context, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show()
            "ERROR_USER_TOKEN_EXPIRED" -> Toast.makeText(context, "The user's credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show()
            "ERROR_USER_NOT_FOUND" -> Toast.makeText(context, "User not found", Toast.LENGTH_LONG).show()
            "ERROR_INVALID_USER_TOKEN" -> Toast.makeText(context, "The user's credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show()
            "ERROR_OPERATION_NOT_ALLOWED" -> Toast.makeText(context, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show()
            "ERROR_WEAK_PASSWORD" -> Toast.makeText(context, "The given password is invalid.", Toast.LENGTH_LONG).show()
        }

    }
    @SuppressLint("ServiceCast")
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun noInternetToast(context: Context){
        Toast.makeText(context,"No Internet", Toast.LENGTH_SHORT).show()
    }

    fun notSignedInToast(context: Context){
        Toast.makeText(context, "You are not signed in", Toast.LENGTH_SHORT).show()
    }

    fun navigateToSignInScreen(context: Context, activity: Activity){
        context.startActivity(Intent(context, SignInScreen::class.java))
        activity.finishAffinity()
        activity.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    fun navigateToResetPwdScreen(context: Context, activity: Activity){
        context.startActivity(Intent(context, ResetPwdScreen::class.java))
        activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }

    fun navigateToHomeScreen(context: Context, activity: Activity){
        context.startActivity(Intent(context, HomeScreen::class.java))
        activity.finishAffinity()
        activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }

    fun visitPrivacyPolicyLink(activity: Activity){
        val url = "https://www.navedansari.in"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
    }

}