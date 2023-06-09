package com.example.login.models

import com.google.firebase.Timestamp

data class Article(
    val articleId : String,
    val authorId : String,
    val title : String,
    val text : String,
    val tags : ArrayList<String>,
    val imageUrl : String,
    val publishedOn : Timestamp,
    val likedBy : ArrayList<String>
)
