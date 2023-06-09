package com.example.login.home

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var homeArticlesRecView : RecyclerView
    private lateinit var articlesList : ArrayList<Article>

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        homeArticlesRecView = view.findViewById(R.id.homeArticlesRecView)
        homeArticlesRecView.layoutManager = LinearLayoutManager(requireContext())
        articlesList = arrayListOf()


        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val articleAdapter = ArticleAdapter(this,articlesList)
        homeArticlesRecView.adapter = articleAdapter

        db.collection("articles").get()
            .addOnSuccessListener {querySnapShot ->
                for(document in querySnapShot){

                    val articleId = document.get("articleId").toString()
                    val authorId = document.get("authorId").toString()
                    val title = document.get("title").toString()
                    val text = document.get("text").toString()
                    val tags = document.get("tags") as ArrayList<String>
                    val imageUrl = document.get("imageUrl").toString()
                    val publishedOn = document.get("publishedOn") as Timestamp
                    val likedBy = document.get("likedBy") as ArrayList<String>

                    articlesList.add(
                        Article(
                            articleId,
                            authorId,
                            title,
                            text,
                            tags,
                            imageUrl,
                            publishedOn,
                            likedBy
                            )
                        )
                    articleAdapter.notifyDataSetChanged()
                }
            }
    }
}