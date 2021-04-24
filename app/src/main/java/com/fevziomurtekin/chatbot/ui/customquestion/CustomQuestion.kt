package com.fevziomurtekin.chatbot.ui.customquestion

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.fevziomurtekin.chatbot.R
import com.fevziomurtekin.chatbot.databinding.ActivityCustomQuestionBinding
import com.fevziomurtekin.chatbot.ui.login.ui.login.LoginActivity
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CustomQuestion : AppCompatActivity(), View.OnClickListener {

    private lateinit var actionBar: ActionBar
    private lateinit var projectId: String
    private var client: SessionsClient? = null
    private var session: SessionName? = null
    private val uuid = UUID.randomUUID().toString()
    private lateinit var viewBinding: ActivityCustomQuestionBinding
    private lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        var currentUser = mAuth.currentUser
        if (checkUser()) {
            var loginIntent = android.content.Intent(this, LoginActivity::class.java)
            startActivityForResult(loginIntent, LoginActivity.USER)
        } else {
            viewBinding = ActivityCustomQuestionBinding.inflate(layoutInflater)
            val view = viewBinding.root
            setContentView(view)
            actionBar = supportActionBar!!
            actionBar.setDisplayHomeAsUpEnabled(true)
            this.projectId = intent.getStringExtra("projectId")
            viewBinding.btnAdd.setOnClickListener(this)
        }

    }

    private fun checkUser() : Boolean{
        return mAuth.currentUser == null
    }

    private fun createNewIntent(
        displayName: String,
        trainingPhrase: List<String>,
        message: List<String>
    ) {
        val parent = ProjectAgentName.of(projectId)
        val trainingPhrasesarray: MutableList<Intent.TrainingPhrase> =
            ArrayList()
        for (trainingPhrases in trainingPhrase) {
            trainingPhrasesarray.add(
                Intent.TrainingPhrase.newBuilder().addParts(
                    Intent.TrainingPhrase.Part.newBuilder()
                        .setText(trainingPhrases).build()
                ).build()
            )
        }
        val messages =
            Intent.Message.newBuilder().setText(
                Intent.Message.Text.newBuilder()
                    .addAllText(message).build()
            ).build()
        val intent =
            Intent.newBuilder()
                .setDisplayName(displayName)
                .addMessages(messages)
                .addAllTrainingPhrases(trainingPhrasesarray)
                .build()
        val stream = resources.openRawResource(R.raw.sivirus)
        val credentials = GoogleCredentials.fromStream(stream)
        var intentsClientSettings = IntentsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build()
        var intentsClient = IntentsClient.create(intentsClientSettings)
        intentsClient.createIntent(parent, intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuth.signOut()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: android.content.Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
            if (requestCode==LoginActivity.USER) {
                if (resultCode == RESULT_OK){
                    if(!checkUser()){
                        viewBinding = ActivityCustomQuestionBinding.inflate(layoutInflater)
                        val view = viewBinding.root
                        setContentView(view)
                        actionBar = supportActionBar!!
                        actionBar.setDisplayHomeAsUpEnabled(true)
                        this.projectId = intent.getStringExtra("projectId")
                        viewBinding.btnAdd.setOnClickListener(this)
                    } else {
                        finish()
                    }
                } else {
                    finish()
                }
            }
    }




    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            viewBinding.btnAdd.id -> {
                val question = viewBinding.edtTextQuestion.text.toString()
                val answer = viewBinding.edtTextAnswer.text.toString()
                val trainingPhrase = listOf<String>(question)
                val message = listOf<String>(answer)
                val randomDisplayName = "Question" + this.uuid
                GlobalScope.launch(Dispatchers.IO) {
                    createNewIntent(randomDisplayName, trainingPhrase, message)
                }
                Toast.makeText(this, "Berhasil Menambahkan pertanyaan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}