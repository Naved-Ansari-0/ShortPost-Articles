package com.example.login.home

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object AddArticleUtils {

    fun uploadImageToFirebase(context: Context, imageUri: Uri, storageLocation: String, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child(storageLocation).child(generateImageName())
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl
                .addOnSuccessListener { uri ->
                    val uploadedImageUrl = uri.toString()
                    callback(uploadedImageUrl)
                }.addOnFailureListener {
                    Toast.makeText(context, "Error on referencing article image url.", Toast.LENGTH_SHORT).show()
                    callback("")
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error on uploading Image", Toast.LENGTH_SHORT).show()
                callback("")
            }
    }


    fun generateImageName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val uid = Firebase.auth.currentUser!!.uid
        return "$uid-$timestamp"
    }

    fun checkTitle(context: Context, title:String):Boolean{
        if(title=="")
            return true
        val namePattern = Regex("^[a-zA-Z0-9@#\$%^&*()\\s\\-_+=!]+\$")
        if(!namePattern.matches(title)){
            Toast.makeText(context,"Invalid character in Title", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkText(context: Context, text:String):Boolean{
        if(text==""){
            Toast.makeText(context,"Content field can't be empty.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkTags(context: Context, tags:String):Boolean{
        if(tags=="")
            return true
        val namePattern = Regex("^[a-zA-Z0-9#]+\$")
        if(!namePattern.matches(tags)){
            Toast.makeText(context,"Invalid character in Tags", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkAbout(context: Context, about:String):Boolean{
        if(about=="")
            return true
        val namePattern = Regex("^[a-zA-Z0-9@*()|';,.\\s-]+\$")
        if(!namePattern.matches(about)){
            Toast.makeText(context,"Invalid character in about", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}