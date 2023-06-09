package com.example.login.models

import com.google.firebase.Timestamp

data class User(
    val name : String,
    val email : String,
    val imageUrl : String,
    val about : String,
    val articles : ArrayList<String>,
    val likedArticles : ArrayList<String>,
    val bookmarkedArticles : ArrayList<String>,
    val accountCreatedOn : Timestamp
)
