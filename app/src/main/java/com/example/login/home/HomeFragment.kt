package com.example.login.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.models.Article
import com.example.login.models.ArticleAdapter
import com.example.login.signIn.SignInSignUpUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class HomeFragment : Fragment() {

    private lateinit var homeArticlesRecView : RecyclerView
    private lateinit var articlesList : ArrayList<Article>
    private lateinit var floatingAddArticleButton : FloatingActionButton
    private lateinit var frameLayout: FrameLayout
    private lateinit var articleAdapter:ArticleAdapter

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    private lateinit var dialog : Dialog
    private val PICK_IMAGE_REQUEST = 123
    private var uploadImagePath : String =  ""

    private var discardArticleButton : ImageButton?=null
    private var articleTitle : TextView?=null
    private var articleText : TextView?=null
    private var articleTags : TextView?=null
    private var addImageButton : ImageButton?=null
    private var imageName : TextView?=null
    private var submitArticleButton : Button?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeArticlesRecView = view.findViewById(R.id.homeArticlesRecView)
        homeArticlesRecView.layoutManager = LinearLayoutManager(requireContext())
        articlesList = arrayListOf()
        floatingAddArticleButton = view.findViewById(R.id.floatingAddArticleButton)
        frameLayout = view.findViewById(R.id.frameLayout)


        val progressBar = ProgressBar(context)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        progressBar.layoutParams = params
        progressBar.isIndeterminate = true
        frameLayout.addView(progressBar)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        articleAdapter = ArticleAdapter(requireContext(),articlesList)
        homeArticlesRecView.adapter = articleAdapter

        db.collection("articles").get()
            .addOnSuccessListener {querySnapShot ->
                for(document in querySnapShot){
                    val status = document.get("status").toString()
                    if(status!="visible")
                        continue
                    val articleId = document.get("articleId").toString()
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
                progressBar.visibility = View.INVISIBLE
            }.addOnFailureListener {
                progressBar.visibility = View.INVISIBLE
            }

        floatingAddArticleButton.setOnClickListener{
            showAddArticleDialog()
        }

    }

    private fun showAddArticleDialog() {

        dialog = Dialog(requireContext(), R.style.TransparentDialog)
        dialog.setContentView(R.layout.add_article)
        dialog.setCancelable(false)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        dialog.window?.attributes = layoutParams
        dialog.show()


        discardArticleButton = dialog.findViewById(R.id.discardArticleButton)
        articleTitle = dialog.findViewById(R.id.articleTitle)
        articleText = dialog.findViewById(R.id.articleText)
        articleTags = dialog.findViewById(R.id.articleTags)
        addImageButton = dialog.findViewById(R.id.addImageButton)
        imageName = dialog.findViewById(R.id.imageName)
        submitArticleButton = dialog.findViewById(R.id.submitArticleButton)


        discardArticleButton!!.setOnClickListener {
            dialog.dismiss()
        }

        addImageButton!!.setOnClickListener {
            uploadImagePath = ""
            if (imageName!!.text == "(Optional)") {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            } else {
                imageName!!.text = "(Optional)"
                addImageButton!!.setImageResource(R.drawable.baseline_photo_size_select_actual_24)
            }
        }

        submitArticleButton!!.setOnClickListener {

            if (!SignInSignUpUtils.isInternetAvailable(requireContext())) {
                SignInSignUpUtils.noInternetToast(requireContext())
                return@setOnClickListener
            }

            val title = articleTitle!!.text.toString().trim()
            val text = articleText!!.text.toString().trim()
            val tags = articleTags!!.text.toString().trim()

            if (!AddArticleUtils.checkTitle(requireContext(), title) ||
                !AddArticleUtils.checkText(requireContext(), text) ||
                !AddArticleUtils.checkTags(requireContext(), tags)
            ) {
                return@setOnClickListener
            }

            discardArticleButton!!.isEnabled = false
            addImageButton!!.isEnabled = false
            submitArticleButton!!.isEnabled = false
            submitArticleButton!!.text = "Submitting..."

            var tagsArray: ArrayList<String> = arrayListOf()
            if (tags.isNotEmpty()) {
                val temp = tags.split(" ", ignoreCase = true, limit = 0)
                for (tag in temp)
                    if(tag.isNotEmpty())
                        tagsArray.add(tag.replace("#",""))
            }

            var uploadedImageUrl = ""
            val authorId = Firebase.auth.currentUser!!.uid
            val articleId = authorId + "-" + UUID.randomUUID().toString()
            val status = "visible"

            val articleData = Article(
                articleId,
                authorId,
                title,
                text,
                tagsArray,
                uploadedImageUrl,
                Timestamp.now(),
                ArrayList(),
                status,
                ArrayList()
            )

            if (uploadImagePath != "") {
                imageName!!.text = "Uploading Image"
                AddArticleUtils.uploadImageToFirebase(requireContext(), Uri.parse(uploadImagePath), "images/articlesImage/"){ it ->
                    if(it != ""){
                        imageName!!.text = "Image Uploaded"
                        articleData.imageUrl = it.toString()
                        submitArticle(articleData)
                    }else {
                    }
                }
            }else{
                submitArticle(articleData)
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            val imageName : TextView = dialog.findViewById(R.id.imageName)
            imageName.text = selectedImageUri!!.path.toString().substringAfterLast("/")
            val addImageButton : ImageButton = dialog.findViewById(R.id.addImageButton)
            addImageButton.setImageResource(R.drawable.baseline_hide_image_24)
            uploadImagePath = selectedImageUri.toString()
        }else{
            uploadImagePath = ""
        }
    }

    private fun submitArticle(article : Article){
        db = FirebaseFirestore.getInstance()
        val articleCollectionRef = db.collection("articles")
        articleCollectionRef.document(article.articleId).set(article)
            .addOnSuccessListener {
                val userCollectionRef = db.collection("users").document(article.authorId)
                userCollectionRef.get()
                    .addOnSuccessListener {documentSnapShot->
                        if(documentSnapShot.exists()){
                            userCollectionRef.update("articles", FieldValue.arrayUnion(article.articleId))
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Submitted successfully", Toast.LENGTH_SHORT).show()
                                    articlesList.add(0,article)
                                    articleAdapter.notifyDataSetChanged()
                                    dialog.dismiss()
                                }.addOnFailureListener {
                                    errorWhileSubmittingArticle()
                                }
                        }else {
                            errorWhileSubmittingArticle()
                        }
                    }.addOnFailureListener {
                        errorWhileSubmittingArticle()
                    }
            }.addOnFailureListener {
                errorWhileSubmittingArticle()
            }
    }

    private fun errorWhileSubmittingArticle(){
        Toast.makeText(requireContext(), "Error while submitting", Toast.LENGTH_SHORT).show()
        discardArticleButton!!.isEnabled = true
        addImageButton!!.isEnabled = true
        addImageButton!!.setImageResource(R.drawable.baseline_photo_size_select_actual_24)
        submitArticleButton!!.isEnabled = true
        imageName!!.text = "(Optional)"
        uploadImagePath = ""
    }

}