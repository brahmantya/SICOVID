    package com.fevziomurtekin.chatbot

    import android.content.ActivityNotFoundException
    import android.content.Intent
    import android.content.res.Configuration
    import android.os.Bundle
    import android.speech.RecognizerIntent
    import android.speech.tts.TextToSpeech
    import android.text.Editable
    import android.view.KeyEvent
    import android.view.LayoutInflater
    import android.view.MenuItem
    import android.view.View
    import android.widget.*
    import androidx.appcompat.app.ActionBarDrawerToggle
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.widget.Toolbar
    import androidx.core.view.GravityCompat
    import androidx.drawerlayout.widget.DrawerLayout
    import com.fevziomurtekin.chatbot.databinding.ActivityMainBinding
    import com.fevziomurtekin.chatbot.databinding.AppBarMainBinding
    import com.fevziomurtekin.chatbot.ui.aboutapp.AboutAppActivity
    import com.fevziomurtekin.chatbot.ui.aboutcreator.AboutCreatorActivity
    import com.fevziomurtekin.chatbot.ui.customquestion.CustomQuestion
    import com.fevziomurtekin.chatbot.ui.reference.ReferenceActivity
    import com.fevziomurtekin.chatbot.ui.reference.ReferenceFragment
    import com.google.android.material.navigation.NavigationView
    import com.google.api.gax.core.FixedCredentialsProvider
    import com.google.auth.oauth2.GoogleCredentials
    import com.google.auth.oauth2.ServiceAccountCredentials
    import com.google.cloud.dialogflow.v2.*
    import kotlinx.android.synthetic.main.activity_main.*
    import kotlinx.android.synthetic.main.app_bar_main.*
    import java.util.*


    const val USER=0
    const val BOT=1
    const val SPEECH_INPUT=2

    class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{



        private var asistan_voice: TextToSpeech? = null
        lateinit var projectID : String

        lateinit var appBarMainBinding: AppBarMainBinding
        lateinit var mainActivityMainBinding: ActivityMainBinding
        lateinit var toolbar: Toolbar
        lateinit var drawerLayout: DrawerLayout
        lateinit var navView: NavigationView
        lateinit var toggle : ActionBarDrawerToggle

        companion object {
         val uuid = UUID.randomUUID().toString()

         var client: SessionsClient? = null
         var session: SessionName? = null
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mainActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(mainActivityMainBinding.root)
            drawerLayout = mainActivityMainBinding.drawerLayout
            toggle = ActionBarDrawerToggle (
                this, drawerLayout,0, 0
            )
            navView = mainActivityMainBinding.navView
            drawerLayout.addDrawerListener(toggle)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            toggle.syncState()
            navView.setNavigationItemSelectedListener (this)

            val scrollview = findViewById<ScrollView>(R.id.scroll_chat)
            scrollview.post {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN)
            }

            val queryEditText = findViewById<EditText>(R.id.edittext)
            queryEditText.setOnKeyListener { view, keyCode, event ->
                if (event.action === KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            sendMessage(send)
                            true
                        }
                        else -> {
                        }
                    }
                }
                false
            }

            send.setOnClickListener(this::sendMessage)

            microphone.setOnClickListener(this::sendMicrophoneMessage)

            initAsisstant()

            initAsisstantVoice()

        }

        override fun onPostCreate(savedInstanceState: Bundle?) {
            super.onPostCreate(savedInstanceState)
            toggle.syncState()
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            toggle.onConfigurationChanged(newConfig)
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            if (toggle.onOptionsItemSelected(item)) {
                return true
            }
            return super.onOptionsItemSelected(item)
        }


        override fun onNavigationItemSelected(item: MenuItem): Boolean {

            when (item.itemId) {
                R.id.nav_home1 -> {
                    val intentReference = Intent(this, ReferenceActivity::class.java)
                    startActivity(intentReference)
                }

                R.id.nav_photos -> {
                   val intentAboutApp = Intent(this, AboutAppActivity::class.java)
                    startActivity(intentAboutApp)
                }

                R.id.nav_movies -> {
                    val intentAboutCreator = Intent(this, AboutCreatorActivity::class.java)
                    startActivity(intentAboutCreator)
                }
                R.id.nav_customquestion -> {
                    val intentCustomQuestion = Intent(this, CustomQuestion::class.java)
                    intentCustomQuestion.putExtra("projectId", this.projectID)
                    startActivity(intentCustomQuestion)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }



        private fun initAsisstantVoice() {

            asistan_voice = TextToSpeech(applicationContext, object : TextToSpeech.OnInitListener {
                override fun onInit(status: Int) {
                    if (status != TextToSpeech.ERROR) {
                        asistan_voice?.language = Locale("id")
                    }
                }

            })

        }

        private fun initAsisstant() {
            try {
                val stream = resources.openRawResource(R.raw.sivirus)
                val credentials = GoogleCredentials.fromStream(stream)
                val projectId = (credentials as ServiceAccountCredentials).projectId
                this.projectID = projectId
                val settingsBuilder = SessionsSettings.newBuilder()
                val sessionsSettings =
                    settingsBuilder.setCredentialsProvider(
                        FixedCredentialsProvider.create(
                            credentials
                        )
                    ).build()
                client = SessionsClient.create(sessionsSettings)
                session = SessionName.of(projectId, uuid)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        private fun sendMessage(view: View) {
            val msg = edittext.text.toString()
            if (msg.trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.mesajgiriniz),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                appendText(msg, USER)
                edittext.setText("")

                // Java V2
                val queryInput =
                    QueryInput.newBuilder()
                        .setText(TextInput.newBuilder().setText(msg).setLanguageCode("id")).build()
                RequestTask(this@MainActivity, session!!, client!!, queryInput).execute()
            }
        }

        private fun sendMicrophoneMessage(view: View) {
            val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt)
            )
            try {
                startActivityForResult(intent, SPEECH_INPUT)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        private fun appendText(message: String, type: Int) {
            val layout: FrameLayout
            when (type) {
                USER -> layout = appendUserText()
                BOT -> layout = appendBotText()
                else -> layout = appendBotText()
            }
            layout.isFocusableInTouchMode = true
            linear_chat.addView(layout)
            val tv = layout.findViewById<TextView>(R.id.chatMsg)
            tv.text = message
            Util.hideKeyboard(this)
            layout.requestFocus()
            edittext.requestFocus() // change focus back to edit text to continue typing
            if (type != USER) asistan_voice?.speak(message, TextToSpeech.QUEUE_FLUSH, null)
        }


        fun appendUserText(): FrameLayout {
            val inflater = LayoutInflater.from(this@MainActivity)
            return inflater.inflate(R.layout.user_message, null) as FrameLayout
        }

        fun appendBotText(): FrameLayout {
            val inflater = LayoutInflater.from(this@MainActivity)
            return inflater.inflate(R.layout.bot_message, null) as FrameLayout
        }

        fun onResult(response: DetectIntentResponse?) {
            try {
                if (response != null) {
                    var botReply: String = ""
                    if (response.queryResult.fulfillmentText == " ")
                        botReply =
                            response.queryResult.fulfillmentMessagesList[0].text.textList[0].toString()
                    else
                        botReply = response.queryResult.fulfillmentText

                    appendText(botReply, BOT)
                } else {
                    appendText(getString(R.string.anlasilmadi), BOT)
                }
            } catch (e: Exception) {
                appendText(getString(R.string.anlasilmadi), BOT)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                SPEECH_INPUT -> {
                    if (resultCode == RESULT_OK
                        && data != null
                    ) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        edittext.text = Editable.Factory.getInstance().newEditable(result[0])
                        sendMessage(send)
                    }
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            if (asistan_voice != null) {
                asistan_voice?.stop()
                asistan_voice?.shutdown()
            }
        }
    }




//            override fun onCreate(savedInstanceState: Bundle?) {
//                super.onCreate(savedInstanceState)
//                setContentView(R.layout.activity_main)


//                setSupportActionBar(toolbar)

//                val toggle = ActionBarDrawerToggle(
//                    this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
//                drawer_layout.addDrawerListener(toggle)
//                toggle.syncState()
//
//                nav_view.setNavigationItemSelectedListener(this)
//
//                displayScreen(-1)
//            }
//
//            override fun onBackPressed() {
//                if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
//                    drawer_layout.closeDrawer(GravityCompat.START)
//                } else {
//                    super.onBackPressed()
//                }
//            }
//
//
//            fun displayScreen(id: Int){
//
//                // val fragment =  when (id){
//
//                when (id){
//                    R.id.nav_home -> {
//                        supportFragmentManager.beginTransaction().replace(R.id.relativelayout, ReferenceFragment()).commit()
//                    }
//
//                    R.id.nav_photos -> {
//                        supportFragmentManager.beginTransaction().replace(R.id.relativelayout, PhotosFragment()).commit()
//                    }
//
//                    R.id.nav_movies -> {
//                        supportFragmentManager.beginTransaction().replace(R.id.relativelayout, AboutAppFragment()).commit()
//                    }
//                }
//            }
//
//            override fun onNavigationItemSelected(item: MenuItem): Boolean {
//                // Handle navigation view item clicks here.
//
//                displayScreen(item.itemId)
//
//                drawer_layout.closeDrawer(GravityCompat.START)
//                return true
//            }


//            }





