package com.entertrainment.photosense

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAPTURE_ACTION_CODE = 1001
    }

    private var mTextToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        controlInfoAndTranslate(false)
        mTextToSpeech = TextToSpeech(applicationContext,
            OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech?.setLanguage(Locale.ENGLISH)
                }
            })

        tvTranslatedText.setMovementMethod(ScrollingMovementMethod())
        setOnClickListener()
    }

    private fun setOnClickListener() {
        fab.setOnClickListener { view ->

            val cInt = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(cInt, CAPTURE_ACTION_CODE)
        }

        tvTo.setOnClickListener {
            val intent = Intent(this@MainActivity, DownloadActivity::class.java)
            intent.putExtra("type", "to")
            startActivityForResult(intent, 22)
        }

        fabSpeak.setOnClickListener {
            tvTranslatedText.text.toString().speak()
        }
    }

    override fun onPause() {
        mTextToSpeech?.stop()
        super.onPause()
    }

    private fun controlInfoAndTranslate(showSpeakOption: Boolean) {
        if (showSpeakOption) {
            fabSpeak.show()
            gpInfo.visibility = View.GONE
        } else {
            fabSpeak.hide()
            tvTranslatedText.text = ""
            ivUserSelected.setImageBitmap(null)
            gpInfo.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_ACTION_CODE && resultCode == Activity.RESULT_OK) {

            controlInfoAndTranslate(true)
            if (data != null) {
                val contentURI: Uri? = data.getData()
                try {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    ivUserSelected.setImageBitmap(bitmap)
                    bitmap?.translateText()

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (resultCode == 22) {
            val name = data?.getStringExtra("model")
            if (data?.getStringExtra("type") == "from") {
                tvFrom.text = name
            } else {
                tvTo.text = name
            }
            controlInfoAndTranslate(false)
        }
    }

    private val mTextDetector = FirebaseVision.getInstance().onDeviceTextRecognizer

    private fun Bitmap.translateText() {
        val firebaseVision = FirebaseVisionImage.fromBitmap(this)
        mTextDetector.processImage(firebaseVision).addOnSuccessListener {

            translateText(it.text)
        }
    }

    private fun translateText(it: String) {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(getLanguageCode(tvFrom.text.toString()))
            .setTargetLanguage(getLanguageCode(tvTo.text.toString()))
            .build()

        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener { _ ->
                translator.translate(it).addOnSuccessListener {
                    tvTranslatedText.text = it
                }
            }
    }

    private fun String.speak() {
        val languageId = when(tvTo.text.toString()) {
            "English" -> "en"
            "Hindi" -> "hin"
            "German" -> "DE"
            "Malay" -> "MS"
            "French" -> "fr"
            else -> "en"
        }

        mTextToSpeech?.setLanguage(Locale.forLanguageTag(languageId))
        mTextToSpeech?.speak(this, TextToSpeech.QUEUE_FLUSH, null);
    }
    private fun getLanguageCode(language: String): Int {
        val languageId = when(language) {
            "English" -> "en"
            "Hindi" -> "hi"
            "German" -> "de"
            "Malay" -> "ms"
            "French" -> "fr"
            else -> "en"
        }

        return FirebaseTranslateLanguage.languageForLanguageCode(languageId)!!

    }
}