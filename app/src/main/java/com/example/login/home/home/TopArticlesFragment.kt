package com.example.login.home.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.models.Article
import com.example.login.models.ArticleAdapter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class TopArticlesFragment : Fragment() {

    private lateinit var topArticlesRecView: RecyclerView
    private lateinit var topArticles: ArrayList<Article>
    private lateinit var topArticlesAdapter: ArticleAdapter

    private lateinit var authorId: String
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recent_articles, container, false)

        topArticlesRecView = view.findViewById(R.id.recentArticlesRecView)
        topArticlesRecView.layoutManager = LinearLayoutManager(requireContext())
        topArticles = arrayListOf()
        topArticlesAdapter = ArticleAdapter(requireContext(), topArticles)
        topArticlesRecView.adapter = topArticlesAdapter

        authorId = arguments?.getString("authorId").toString()

        return view
    }

    override fun onResume() {
        super.onResume()

        fetchTopArticles()
    }

    private fun fetchTopArticles() {
        db = FirebaseFirestore.getInstance()
        db.collection("articles").get()
            .addOnSuccessListener { querySnapshot ->
                topArticles.clear()
                for (document in querySnapshot) {
                    val articleAuthorId = document.get("authorId").toString()
                    val status = document.get("status").toString()
                    if (status != "visible" || articleAuthorId != authorId)
                        continue
                    val articleId = document.get("articleId").toString()
                    val title = document.get("title").toString()
                    val text = document.get("text").toString()
                    val tags = document.get("tags") as ArrayList<String>
                    val imageUrl = document.get("imageUrl").toString()
                    val publishedOn = document.get("publishedOn") as Timestamp
                    val likedBy = document.get("likedBy") as ArrayList<String>
                    val reportedBy = document.get("reportedBy") as ArrayList<String>

                    topArticles.add(
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
                }
                topArticles.sortByDescending { it.likedBy.size }
                topArticlesAdapter.notifyDataSetChanged()
            }
    }
}
