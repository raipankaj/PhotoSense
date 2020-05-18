package com.entertrainment.photosense

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.entertrainment.photosense.adapter.DownloadLanguageAdapter
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel
import kotlinx.android.synthetic.main.activity_download.*


class DownloadActivity : AppCompatActivity(R.layout.activity_download) {

    private var mModelDownloaded = false
    private val mLanguageArray = arrayOf(
        Pair("English", FirebaseTranslateLanguage.EN),
        Pair("Hindi", FirebaseTranslateLanguage.HI),
        Pair("German", FirebaseTranslateLanguage.DE),
        Pair("Malay", FirebaseTranslateLanguage.MS),
        Pair("French", FirebaseTranslateLanguage.FR),
        Pair("Spanish", FirebaseTranslateLanguage.ES)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rvLanguage.adapter = DownloadLanguageAdapter(mLanguageArray, mOnLanguageClickListener)
    }

    private val mModelManager = FirebaseModelManager.getInstance()

    private val mOnLanguageClickListener = object : DownloadLanguageAdapter.OnLanguageSelected {
        override fun onClick(name: String, id: Int) {

            val model = FirebaseTranslateRemoteModel.Builder(id).build()
            mModelManager.isModelDownloaded(model).addOnSuccessListener {
                if (it) {
                    sendModelName(name)
                } else {
                    Toast.makeText(this@DownloadActivity, "Please wait until model is getting downloaded", Toast.LENGTH_LONG).show()
                    mCountDownTimer.start()

                    mModelManager.download(model, FirebaseModelDownloadConditions.Builder().build())
                        .addOnSuccessListener {
                            mModelDownloaded = true
                            mCountDownTimer.cancel()
                            sendModelName(name)
                        }
                }
            }
        }
    }

    private val mCountDownTimer = object : CountDownTimer(50_000,1) {
        override fun onFinish() {
            if (mModelDownloaded.not()) {
                Toast.makeText(this@DownloadActivity, "Unable to download model, please try again later", Toast.LENGTH_LONG).show()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            if (millisUntilFinished % 500 == 0L) {
                tvDownloading.visibility = if (tvDownloading.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
    }

    private fun sendModelName(name: String) {
        val intent = Intent()
        intent.putExtra("model", name)
        intent.putExtra("type", getIntent().getStringExtra("type"))
        setResult(22, intent)
        finish()
    }
}