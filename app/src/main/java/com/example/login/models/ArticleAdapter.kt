package com.example.login.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.R
import com.example.login.home.home.OthersProfile
import com.example.login.signIn.SignInSignUpUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ArticleAdapter(
        private val context: Context,
        private val articlesList: ArrayList<Article>
        ):RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>(){

        class ArticleViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
                val authorImage : CircleImageView = itemView.findViewById(R.id.authorImage)
                val authorName : TextView = itemView.findViewById(R.id.authorName)
                val deleteArticleButton : ImageButton = itemView.findViewById(R.id.deleteArticleButton)
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
                val authorId = articlesList[position].authorId
                val articleDocumentRef = db.collection("articles").document(articleId)
                val authorDocumentRef = db.collection("users").document(authorId)
                val userDocumentRef = db.collection("users").document(userId)

                authorDocumentRef.get().addOnSuccessListener{documentSnapShot ->
                        if(documentSnapShot.exists()){
                                val authorName = documentSnapShot.get("name").toString()
                                val authorImageUrl = documentSnapShot.get("imageUrl").toString()
                                holder.authorName.text = authorName
                                if(authorImageUrl=="")
                                        holder.authorImage.setImageResource(R.drawable.default_user_image)
                                else
                                        Glide.with(context).load(authorImageUrl).into(holder.authorImage)
                        }
                }

                holder.authorName.setOnClickListener {
                        val intent = Intent(context, OthersProfile::class.java)
                        intent.putExtra("authorId", authorId)
                        context.startActivity(intent)
                }
                holder.authorImage.setOnClickListener {
                        val intent = Intent(context, OthersProfile::class.java)
                        intent.putExtra("authorId", authorId)
                        context.startActivity(intent)
                }
                if(context is OthersProfile || authorId==userId){
                        holder.authorName.isClickable = false
                        holder.authorImage.isClickable = false
                }else{
                        holder.authorName.isClickable = true
                        holder.authorImage.isClickable = true
                }

                if(authorId!=userId){
                        holder.deleteArticleButton.setImageResource(R.drawable.outline_report_24)
                }else{
                        holder.deleteArticleButton.setImageResource(R.drawable.baseline_delete_outline_24)
                }

                holder.deleteArticleButton.setOnClickListener {
                        if(!SignInSignUpUtils.isInternetAvailable(context)){
                                SignInSignUpUtils.noInternetToast(context)
                                return@setOnClickListener
                        }

                        holder.deleteArticleButton.isEnabled = false
                        articleDocumentRef.get().addOnSuccessListener {documentSnapShot->
                                if(documentSnapShot.exists()){
                                        if(authorId==userId){
                                                val builder = AlertDialog.Builder(context)
                                                builder.setMessage("Are you sure you want to delete this article?")
                                                builder.setPositiveButton("Yes") { dialog, _ ->
                                                        builder.setCancelable(false)
                                                        articleDocumentRef.update("status", "deleted")
                                                                .addOnSuccessListener {
                                                                        articlesList.removeAt(position)
                                                                        notifyItemRemoved(position)
                                                                        notifyItemRangeChanged(position, articlesList.size)
                                                                        holder.deleteArticleButton.isEnabled = true
                                                                        dialog.dismiss()
                                                                }.addOnFailureListener {
                                                                        holder.deleteArticleButton.isEnabled = true
                                                                        dialog.dismiss()
                                                                }
                                                }
                                                val dialog = builder.create()
                                                dialog.show()
                                        }else{
                                                if(articlesList[position].reportedBy.contains(userId)){
                                                        Toast.makeText(context, "Already reported", Toast.LENGTH_SHORT).show()
                                                        return@addOnSuccessListener
                                                }
                                                val builder = AlertDialog.Builder(context)
                                                builder.setMessage("Do you want to report this article for inappropriate content?")
                                                builder.setPositiveButton("Yes") { dialog, _ ->
                                                        builder.setCancelable(false)
                                                        articleDocumentRef.update("reportedBy", FieldValue.arrayUnion(userId))
                                                                .addOnSuccessListener {
                                                                        Toast.makeText(context, "Reported", Toast.LENGTH_SHORT).show()
                                                                        articlesList[position].reportedBy.add(userId)
                                                                        holder.deleteArticleButton.isEnabled = true
                                                                        dialog.dismiss()
                                                                }.addOnFailureListener {
                                                                        holder.deleteArticleButton.isEnabled = true
                                                                        dialog.dismiss()
                                                                }
                                                }
                                                val dialog = builder.create()
                                                dialog.show()
                                        }
                                }else
                                        holder.deleteArticleButton.isEnabled = true
                        }.addOnFailureListener {
                                holder.deleteArticleButton.isEnabled = true
                        }
                }

                if (articlesList[position].title.isBlank()) {
                        holder.articleTitle.visibility = View.GONE
                } else {
                        holder.articleTitle.visibility = View.VISIBLE
                        holder.articleTitle.text = articlesList[position].title
                }

                holder.articleText.text = articlesList[position].text

                var tagText = ""
                for(tag in articlesList[position].tags)
                        tagText  += "#$tag "
                holder.articleTag.text = tagText
                if(tagText=="")
                        holder.articleTag.visibility = View.GONE
                else{
                        holder.articleTag.text = tagText
                        holder.articleTag.visibility = View.VISIBLE
                }


                if(articlesList[position].imageUrl=="")
                        holder.articleImage.visibility = View.GONE
                else {
                        holder.articleImage.visibility = View.VISIBLE
                        Glide.with(context).load(articlesList[position].imageUrl).into(holder.articleImage)
                }

                holder.articleLikeCount.text = articlesList[position].likedBy.size.toString()
                if(articlesList[position].likedBy.isEmpty())
                        holder.articleLikeCount.visibility = View.INVISIBLE
                else {
                        holder.articleLikeCount.visibility = View.VISIBLE
                        holder.articleLikeCount.text = articlesList[position].likedBy.size.toString()
                }

                if(articlesList[position].likedBy.contains(userId))
                        holder.likeArticleButton.setImageResource(R.drawable.baseline_thumb_up_24)
                else
                        holder.likeArticleButton.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)

                val date = articlesList[position].publishedOn.toDate()
                val dateFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(date)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                holder.articlePublishedOn.text = "$dateFormat at $timeFormat"


                holder.bookmarkArticleButton.setImageResource(R.drawable.baseline_bookmark_add_24)
                userDocumentRef.get().addOnSuccessListener { documentSnapShot ->
                        if (documentSnapShot.exists()) {
                                val bookmarkedArticles = documentSnapShot.get("bookmarkedArticles") as ArrayList<*>
                                if (bookmarkedArticles.contains(articleId))
                                        holder.bookmarkArticleButton.setImageResource(R.drawable.baseline_bookmark_added_24)
                        }
                }

                holder.likeArticleButton.setOnClickListener {
                        if(!SignInSignUpUtils.isInternetAvailable(context)){
                                SignInSignUpUtils.noInternetToast(context)
                                return@setOnClickListener
                        }

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
                        if(!SignInSignUpUtils.isInternetAvailable(context)){
                                SignInSignUpUtils.noInternetToast(context)
                                return@setOnClickListener
                        }

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


                holder.articleImage.setOnClickListener {
                        val scaleAnimation = ScaleAnimation(1f, 0.95f, 1f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                        scaleAnimation.duration = 500
                        holder.articleImage.startAnimation(scaleAnimation)
                }

        }
}