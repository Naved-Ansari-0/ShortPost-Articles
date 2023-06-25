package com.example.login.home.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.models.Article
import com.example.login.models.ArticleAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MyArticleScreen : AppCompatActivity() {

    private lateinit var othersArticlesRecView : RecyclerView
    private lateinit var articlesList : ArrayList<Article>
    private lateinit var frameLayout: FrameLayout
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var progressBar: ProgressBar

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_article_screen)

        othersArticlesRecView = findViewById(R.id.othersArticlesRecView)
        othersArticlesRecView.layoutManager = LinearLayoutManager(this)
        articlesList = arrayListOf()
        frameLayout = findViewById(R.id.frameLayout)

        progressBar = ProgressBar(this)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        progressBar.layoutParams = params
        progressBar.isIndeterminate = true
        frameLayout.addView(progressBar)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val articleType = intent.extras!!.getString("articleType").toString()
        loadOthersArticles(articleType)
    }


    private fun loadOthersArticles(articleType:String){
        articleAdapter = ArticleAdapter(this,articlesList)
        othersArticlesRecView.adapter = articleAdapter

        val userId = auth.currentUser!!.uid
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { documentSnapShot->
                if(documentSnapShot.exists()){
                    val articles = documentSnapShot.get(articleType) as ArrayList<String>
                    if(articles.isNotEmpty()) {
                        db.collection("articles").get()
                            .addOnSuccessListener { querySnapShot ->
                                for (document in querySnapShot) {
                                    val articleId = document.get("articleId").toString()
                                    val status = document.get("status").toString()
                                    if (!articles.contains(articleId) || status!="visible")
                                        continue
                                    val authorId = document.get("authorId").toString()
                                    val title = document.get("title").toString()
                                    val text = document.get("text").toString()
                                    val tags = document.get("tags") as ArrayList<String>
                                    val imageUrl = document.get("imageUrl").toString()
                                    val publishedOn = document.get("publishedOn") as Timestamp
                                    val likedBy = document.get("likedBy") as ArrayList<String>
                                    val reportedBy = document.get("reportedBy") as ArrayList<String>
                                    articlesList.add(
                                        Article(
                                            articleId,
                                            authorId,
                                            title,
                                            text,
                                            tags,
                                            imageUrl,
                                            publishedOn,
                                            likedBy,
                                            status,
                                            reportedBy
                                        )
                                    )
                                    articleAdapter.notifyDataSetChanged()
                                }
                                articlesList.sortByDescending { it.publishedOn }
                                if(articlesList.isEmpty())
                                    Toast.makeText(this, "No article to show", Toast.LENGTH_SHORT).show()
                                progressBar.visibility = View.INVISIBLE
                            }.addOnFailureListener {
                                progressBar.visibility = View.INVISIBLE
                            }
                    } else {
                        Toast.makeText(this, "No article to show", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            }.addOnCanceledListener {
                progressBar.visibility = View.INVISIBLE
            }
    }


}