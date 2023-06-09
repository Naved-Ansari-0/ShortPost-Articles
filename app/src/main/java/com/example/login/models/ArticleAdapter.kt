package com.example.login.models

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.R
import com.example.login.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ArticleAdapter(
        private val context: HomeFragment,
        private val articlesList: ArrayList<Article>
        ):RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>(){

        class ArticleViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
                val authorImage : CircleImageView = itemView.findViewById(R.id.authorImage)
                val authorName : TextView = itemView.findViewById(R.id.authorName)
                val articleTitle : TextView = itemView.findViewById(R.id.articleTitle)
                val articleText : TextView = itemView.findViewById(R.id.articleText)
                val articleTag : TextView = itemView.findViewById(R.id.articleTag)
                val articleImage : ImageView = itemView.findViewById(R.id.articleImage)
                val likeArticleButton : ImageButton = itemView.findViewById(R.id.likeArticleButton)
                val articleLikeCount : TextView = itemView.findViewById(R.id.articleLikeCount)
                val bookmarkArticleButton : ImageButton = itemView.findViewById(R.id.bookmarkArticleButton)
                val articlePublishedOn : TextView = itemView.findViewById(R.id.articlePublishedOn)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.article_post, parent, false)
                return ArticleViewHolder(itemView)
        }

        override fun getItemCount(): Int {
                return articlesList.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {

                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                val db = FirebaseFirestore.getInstance()
                val articleId = articlesList[position].articleId
                val articleDocumentRef = db.collection("articles").document(articleId)
                val authorDocumentRef = db.collection("users").document(articlesList[position].authorId)
                val userDocumentRef = db.collection("users").document(userId)

                authorDocumentRef.get().addOnSuccessListener{documentSnapShot ->
                        if(documentSnapShot.exists()){
                                val authorName = documentSnapShot.get("name").toString()
                                val authorImageUrl = documentSnapShot.get("imageUrl").toString()
                                holder.authorName.text = authorName
                                if(authorImageUrl=="")
                                        holder.authorImage.setImageResource(R.drawable.baseline_person_24)
                                else
                                        Glide.with(context).load(authorImageUrl).into(holder.authorImage)
                        }
                }

                if(articlesList[position].title=="")
                        holder.articleTitle.visibility = View.INVISIBLE
                else
                        holder.articleTitle.text = articlesList[position].title

                holder.articleText.text = articlesList[position].text

                var tagText = ""
                for(tag in articlesList[position].tags)
                        tagText  += "#$tag "
                holder.articleTag.text = tagText
                if(tagText=="")
                        holder.articleTag.visibility = View.INVISIBLE

                if(articlesList[position].imageUrl=="")
                        holder.authorImage.visibility = View.INVISIBLE
                else
                        Glide.with(context).load(articlesList[position].imageUrl).into(holder.articleImage)

                holder.articleLikeCount.text = articlesList[position].likedBy.size.toString()
                if(articlesList[position].likedBy.isEmpty())
                        holder.articleLikeCount.visibility = View.INVISIBLE
                if(articlesList[position].likedBy.contains(userId))
                        holder.likeArticleButton.setImageResource(R.drawable.baseline_thumb_up_24)

                val date = articlesList[position].publishedOn.toDate()
                val dateFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(date)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                holder.articlePublishedOn.text = "$dateFormat at $timeFormat"

                userDocumentRef.get().addOnSuccessListener { documentSnapShot ->
                        if (documentSnapShot.exists()) {
                                val bookmarkedArticles = documentSnapShot.get("bookmarkedArticles") as ArrayList<*>
                                if (bookmarkedArticles.contains(articleId))
                                        holder.bookmarkArticleButton.setImageResource(R.drawable.baseline_bookmark_added_24)
                        }
                }



                holder.likeArticleButton.setOnClickListener {
                        holder.likeArticleButton.isEnabled = false
                        userDocumentRef.get().addOnSuccessListener { documentSnapShot->
                                if(documentSnapShot.exists()){
                                        val likedArticles = documentSnapShot.get("likedArticles") as ArrayList<*>
                                        if(likedArticles.contains(articleId)){
                                                userDocumentRef.update("likedArticles", FieldValue.arrayRemove(articleId))
                                                        .addOnSuccessListener {
                                                                articleDocumentRef.get().addOnSuccessListener {documentSnapShot->
                                                                        if(documentSnapShot.exists()){
                                                                                articleDocumentRef.update("likedBy", FieldValue.arrayRemove(userId))
                                                                                        .addOnSuccessListener {
                                                                                                holder.likeArticleButton.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                                                                                                val likesCount = holder.articleLikeCount.text.toString().toInt() - 1
                                                                                                holder.articleLikeCount.text = likesCount.toString()
                                                                                                if(likesCount==0)
                                                                                                        holder.articleLikeCount.visibility = View.INVISIBLE
                                                                                                articlesList[position].likedBy.remove(userId)
                                                                                                holder.likeArticleButton.isEnabled = true
                                                                                        }.addOnFailureListener {
                                                                                                holder.likeArticleButton.isEnabled = true
                                                                                        }
                                                                        }else
                                                                                holder.likeArticleButton.isEnabled = true
                                                                }
                                                        }.addOnFailureListener {
                                                                holder.likeArticleButton.isEnabled = true
                                                        }
                                        }else{
                                                userDocumentRef.update("likedArticles", FieldValue.arrayUnion(articleId))
                                                        .addOnSuccessListener {
                                                                articleDocumentRef.get().addOnSuccessListener {documentSnapShot->
                                                                        if(documentSnapShot.exists()){
                                                                                articleDocumentRef.update("likedBy", FieldValue.arrayUnion(userId))
                                                                                        .addOnSuccessListener {
                                                                                                holder.likeArticleButton.setImageResource(R.drawable.baseline_thumb_up_24)
                                                                                                val likesCount = holder.articleLikeCount.text.toString().toInt() + 1
                                                                                                holder.articleLikeCount.text = likesCount.toString()
                                                                                                if(likesCount==1)
                                                                                                        holder.articleLikeCount.visibility = View.VISIBLE
                                                                                                articlesList[position].likedBy.add(userId)
                                                                                                holder.likeArticleButton.isEnabled = true
                                                                                        }.addOnFailureListener {
                                                                                                holder.likeArticleButton.isEnabled = true
                                                                                        }
                                                                        }else
                                                                                holder.likeArticleButton.isEnabled = true
                                                                }
                                                        }.addOnFailureListener {
                                                                holder.likeArticleButton.isEnabled = true
                                                        }
                                        }
                                }else
                                        holder.likeArticleButton.isEnabled = true
                        }.addOnFailureListener {
                                holder.likeArticleButton.isEnabled = true
                        }
                }



                holder.bookmarkArticleButton.setOnClickListener {
                        holder.bookmarkArticleButton.isEnabled = false
                        userDocumentRef.get().addOnSuccessListener {documentSnapShot->
                                if(documentSnapShot.exists()){
                                        val bookmarkedArticles = documentSnapShot.get("bookmarkedArticles") as ArrayList<*>
                                        if(bookmarkedArticles.contains(articleId)){
                                                userDocumentRef.update("bookmarkedArticles",FieldValue.arrayRemove(articleId))
                                                        .addOnSuccessListener {
                                                                holder.bookmarkArticleButton.setImageResource(R.drawable.baseline_bookmark_add_24)
                                                                holder.bookmarkArticleButton.isEnabled = true
                                                        }.addOnFailureListener {
                                                                holder.bookmarkArticleButton.isEnabled = true
                                                        }
                                        }else{
                                                userDocumentRef.update("bookmarkedArticles",FieldValue.arrayUnion(articleId))
                                                        .addOnSuccessListener {
                                                                holder.bookmarkArticleButton.setImageResource(R.drawable.baseline_bookmark_added_24)
                                                                holder.bookmarkArticleButton.isEnabled = true
                                                        }.addOnFailureListener{
                                                                holder.bookmarkArticleButton.isEnabled = true
                                                        }
                                        }
                                }else
                                        holder.bookmarkArticleButton.isEnabled = true
                        }.addOnFailureListener {
                                holder.bookmarkArticleButton.isEnabled = true
                        }
                }

        }
}