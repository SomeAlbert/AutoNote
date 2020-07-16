package com.example.autoconspect

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_sr.*
import org.kaldi.Assets
import org.kaldi.Model
import org.kaldi.RecognitionListener
import org.kaldi.SpeechRecognizer
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference


class SRActivity : AppCompatActivity(), RecognitionListener {
    companion object {
        const val TAG = "STT"
        const val permissionRequestCode = 102
        const val STATE_START = 0
        const val STATE_READY = 1
        const val STATE_DONE = 2
        const val STATE_MIC = 3
    }

    init {
        System.loadLibrary("kaldi_jni")
    }

    private var sr: SpeechRecognizer? = null
    private lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sr)

        setUiState(STATE_START)
        start_listener.setOnClickListener {
            recognizeMicro()
        }

        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequestCode
            )
        }

        SetupTask(this).execute()
    }

    private class SetupTask(activity: SRActivity) : AsyncTask<Void, Void, Exception>() {
        var activityReference: WeakReference<SRActivity>? = null

        init {
            this.activityReference = WeakReference<SRActivity>(activity)
        }

        override fun doInBackground(vararg params: Void?): Exception? {
            try {
                val assets = Assets(activityReference?.get())
                val assetDir: File = assets.syncAssets()
                Log.d(TAG, "Sync files in folder: $assetDir")

                // Vosk.SetLogLevel(0)

                activityReference?.get()?.model = Model("$assetDir/model-small-ru") //fixme model
            } catch (e: IOException) {
                return e
            }
            return null
        }

        override fun onPostExecute(result: Exception?) {
            if (result != null) {
                activityReference?.get()
                    ?.setErrorState("Failed to initialize the recognizer $result")
            } else run {
                activityReference?.get()?.setUiState(STATE_READY)
            }
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
                    SetupTask(this).execute()
                } else {
                    finish()
                }
            }
            else -> {
                Log.d("STT", "wrong permission response code")
                finish()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sr?.cancel()
        sr?.shutdown()
    }

    override fun onResult(p0: String?) {
        speechView.append("$p0\n")
    }

    override fun onPartialResult(p0: String?) {}

    override fun onError(p0: java.lang.Exception?) {
        p0?.message?.let { setErrorState(it) }
    }

    override fun onTimeout() {
        sr?.cancel()
        sr = null
        setUiState(STATE_READY)
    }

    @SuppressLint("SetTextI18n")
    private fun setUiState(state: Int) {
        when (state) {
            STATE_START -> {
                infoView.text = "Preparing the recognizer"
                speechView.movementMethod = ScrollingMovementMethod()
                start_listener.isEnabled = false
            }
            STATE_READY -> {
                infoView.text = "ready"
                start_listener.text = "Recognize micro"
                start_listener.isEnabled = true
            }
            STATE_DONE -> {
                start_listener.text = "recognize micro"
                start_listener.isEnabled = true
            }
            STATE_MIC -> {
                start_listener.text = "stop micro"
                infoView.text = "Say something"
                start_listener.isEnabled = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setErrorState(message: String) {
        infoView.text = message
        start_listener.text = "recognize micro"
        start_listener.isEnabled = false
    }

    private fun recognizeMicro() {
        if (sr != null) {
            setUiState(STATE_DONE)
            sr?.cancel()
            sr = null
        } else {
            setUiState(STATE_MIC)
            try {
                sr = SpeechRecognizer(model)
                sr?.addListener(this)
                sr?.startListening()
            } catch (e: IOException) {
                e.message?.let { setErrorState(it) }
            }
        }
    }

}
