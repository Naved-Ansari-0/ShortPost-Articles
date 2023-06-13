package com.example.login.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.bumptech.glide.Glide
import com.example.login.R
import com.example.login.signIn.SignInSignUpUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var settingMenuButton : ImageButton
    private lateinit var editDetailsButton : ImageButton
    private lateinit var userImage : CircleImageView
    private lateinit var userName : TextView
    private lateinit var aboutUser : TextView
    private lateinit var myArticles : TextView
    private lateinit var likedArticles : TextView
    private lateinit var bookmarkedArticles : TextView


    private var userNameEditText : EditText?=null
    private var aboutUserEditText : EditText?=null
    private var saveDetailsButton : Button?=null

    private lateinit var userId : String
    private lateinit var db : FirebaseFirestore

    private lateinit var detailsLinearLayout : LinearLayout
    private lateinit var articlesMenuLinearLayout : LinearLayout


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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingMenuButton = view.findViewById(R.id.settingMenuButton)
        editDetailsButton = view.findViewById(R.id.editDetailsButton)
        userImage = view.findViewById(R.id.userImage)
        userName = view.findViewById(R.id.userName)
        aboutUser = view.findViewById(R.id.aboutUser)
        myArticles = view.findViewById(R.id.myArticles)
        likedArticles = view.findViewById(R.id.likedArticles)
        bookmarkedArticles = view.findViewById(R.id.bookmarkedArticles)

        detailsLinearLayout = view.findViewById(R.id.detailsLinearLayout)
        articlesMenuLinearLayout = view.findViewById(R.id.articlesMenuLinearLayout)
        detailsLinearLayout.visibility = View.GONE
        articlesMenuLinearLayout.visibility = View.GONE


        settingMenuButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingScreen::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }

        editDetailsButton.setOnClickListener {
            editDetailsDialog()
        }

        userId = Firebase.auth.currentUser!!.uid
        db = FirebaseFirestore.getInstance()
        val userDocumentRef = db.collection("users").document(userId)
        userDocumentRef.get()
            .addOnSuccessListener {documentSnapShot->
                if(documentSnapShot.exists()){
                    val name = documentSnapShot.get("name").toString()
                    val about = documentSnapShot.get("about").toString()
                    val imageUrl = documentSnapShot.get("imageUrl").toString()
                    userName.text = name
                    aboutUser.text = about
                    if(imageUrl!="")
                        Glide.with(requireContext()).load(imageUrl).into(userImage)
                    else {
                        userImage.setImageResource(R.drawable.baseline_person_24)
                    }
                    detailsLinearLayout.visibility = View.VISIBLE
                    articlesMenuLinearLayout.visibility = View.VISIBLE
                }
            }.addOnFailureListener {

            }

        val clickListener = View.OnClickListener { view->
            val intent = Intent(requireContext(), MyArticleScreen::class.java)
            when(view.id){
                R.id.myArticles-> intent.putExtra("articleType", "articles")
                R.id.bookmarkedArticles-> intent.putExtra("articleType", "bookmarkedArticles")
                R.id.likedArticles-> intent.putExtra("articleType", "likedArticles")
            }
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }

        myArticles.setOnClickListener(clickListener)
        bookmarkedArticles.setOnClickListener(clickListener)
        likedArticles.setOnClickListener(clickListener)


    }

    private fun editDetailsDialog(){
        val dialog = Dialog(requireContext(), R.style.TransparentDialog)
        dialog.setContentView(R.layout.edit_details)
        dialog.setCancelable(true)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        dialog.window?.attributes = layoutParams
        dialog.show()

        userNameEditText = dialog.findViewById(R.id.userNameEditText)
        aboutUserEditText = dialog.findViewById(R.id.aboutUserEditText)
        saveDetailsButton = dialog.findViewById(R.id.saveDetailsButton)

        userNameEditText?.setText(userName.text)
        aboutUserEditText?.setText(aboutUser.text)

        saveDetailsButton?.setOnClickListener {

            if(!SignInSignUpUtils.isInternetAvailable(requireContext())){
                SignInSignUpUtils.noInternetToast(requireContext())
                return@setOnClickListener
            }

            val name = userNameEditText?.text.toString().trim()
            val about = aboutUserEditText?.text.toString().trim()

            if(!SignInSignUpUtils.checkName(requireContext(), name) ||
                    !AddArticleUtils.checkAbout(requireContext(),about))
                return@setOnClickListener

            dialog.setCancelable(false)
            saveDetailsButton?.text = "Saving"

            userId = Firebase.auth.currentUser!!.uid
            db = FirebaseFirestore.getInstance()
            val userDocumentRef = db.collection("users").document(userId)

            val editedDetails = mapOf(
                "name" to name,
                "about" to about
            )

            userDocumentRef.update(editedDetails)
                .addOnSuccessListener{
                    Toast.makeText(requireContext(), "Details updated successfully", Toast.LENGTH_SHORT).show()
                    userName.text = name
                    aboutUser.text = about
                    dialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Error on updating details", Toast.LENGTH_SHORT).show()
                    saveDetailsButton?.text = "Save"
                    dialog.setCancelable(true)
                }
        }
    }


}

