package com.example.autoconspect

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.createSpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.content.Context
import android.webkit.PermissionRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.security.Permission
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private lateinit var sr: SpeechRecognizer
    private val permissionRequestCode = 102
    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO
    )
    val TAG = "STT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, permissions, permissionRequestCode)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
        start_listener.setOnClickListener {
            sr = createSpeechRecognizer(this)
            sr.setRecognitionListener(RecListener())
            sr.startListening(intent)
        }
    }

        class RecListener : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("STT", "ReadyForSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBeginningOfSpeech() {}

            override fun onEndOfSpeech() {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onError(error: Int) {
                val errorText: String = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error Audio"
                    SpeechRecognizer.ERROR_CLIENT -> " Error client"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission error"
                    SpeechRecognizer.ERROR_NETWORK -> "Networl error"
                    else -> "unknown error" //todo finish this
                }
                Log.e("STT", "Error: $errorText")
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onResults(results: Bundle?) {
                val matches: ArrayList<String> =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                var res : String = ""
                for (i in 1..matches.size) {
                    res += i.toString()
                }
                Log.d("STTRES", res)
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permissionRequestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //use it
                } else {
                    //oops
                }
            }
            else -> {//wrong id
            }
        }

    }
}
// is it working