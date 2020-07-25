package com.example.autoconspect

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_sr.*
import org.kaldi.Assets
import org.kaldi.Model
import org.kaldi.RecognitionListener
import org.kaldi.SpkModel
import java.io.File
import java.io.IOException
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference


class SRActivity : AppCompatActivity(), RecognitionListener {
    companion object {
        const val TAG = "AutoConspect"
        const val permissionRequestCode = 102
        const val STATE_START = 0
        const val STATE_READY = 1
        const val STATE_DONE = 2
        const val STATE_MIC = 3
        var modelName = "rus"
        var spkModelPath = "model-spk"
        val models = mapOf(
            "rus" to "model-small-ru",
            "eng" to "model-android"
        ) //add model here
    }

    private var sr: SpkSpcRecognizer? = null

    private lateinit var model: Model
    private lateinit var spkModel: SpkModel
    private var gson = Gson()
    var savePath: File? = null
    lateinit var activityReference: SoftReference<SRActivity>
    //fixme куда сохранять текст (по идее это пусть внутри приложения (не абсолютный), но это не точно
    init {
        System.loadLibrary("kaldi_jni")

    }


    override  fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sr)

        setUiState(STATE_START)
        start_listener.setOnClickListener {
            recognizeMicro()
        }
        saveFile.setOnClickListener {
            saveFile()
        }

        checkPermissions()

        activityReference = SoftReference<SRActivity>(this)
        savePath = activityReference.get()?.applicationContext?.dataDir

        //if (savePath?.exists() == false) { this.savePath!!.mkdir()}
        SetupTask(this).execute()
    }


    private inner class SetupTask(activity: SRActivity) : AsyncTask<Void, Void, Exception>() {
        var activityReferenceWeak: WeakReference<SRActivity>? = null

        init {
            activityReferenceWeak = WeakReference<SRActivity>(activity)
        }

        override fun doInBackground(vararg params: Void?): Exception? {
            try {
                val assets = Assets(activityReferenceWeak?.get())
                val assetDir: File = assets.syncAssets()
                Log.d(TAG, "Sync files in folder: $assetDir")
                // Vosk.SetLogLevel(0)
                activityReferenceWeak?.get()?.model = Model("$assetDir/${models[modelName]}")
                activityReferenceWeak?.get()?.spkModel = SpkModel("$assetDir/$spkModelPath")
            } catch (e: IOException) {
                return e
            }
            return null
        }

        override fun onPostExecute(result: Exception?) {
            if (result != null) {
                activityReferenceWeak?.get()
                    ?.setErrorState("Failed to initialize the recognizer $result")
            } else run {
                activityReferenceWeak?.get()?.setUiState(STATE_READY)
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
        if (p0 != null) {
            try {
                val json = gson.fromJson(p0, JsonObject::class.java)
                val text = json.get("text").asString
                val spk = json.get("spk").asJsonArray.toMutableList()
                var spkSum = 0f
//                val result = if (json.has("result")) json.get("result").asJsonArray[0].asJsonObject else null
//                if ((result != null) && (resultText != "")) {
//                    speechView.setText(speechView.text.replace(Regex("$resultText(\\s|\\s\\s|\\s\\s\\s)*\$"), "  ${result.get("word").asString}")) //fixme result
//                } else {
//                    speechView.append(text)
//                    resultText += "$text "
//                    infoView.text = resultText
//                }
                speechView.append(" $text")
                for (i in spk) {
                    spkSum += i.asFloat
                }
                val spkMean = spkSum / spk.size
                spkInfo.text = "Speaker mean: $spkMean"
                //speechView.append("$p0\n")
            } catch (e: Exception) {
                Toast.makeText(this@SRActivity, "$e", Toast.LENGTH_SHORT).show()
            }
        }
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
                //infoView.text = "Preparing the recognizer"
                speechView.movementMethod = ScrollingMovementMethod()
                start_listener.isEnabled = false
            }
            STATE_READY -> {
                //infoView.text = "ready"
                start_listener.text = "Recognize micro"
                start_listener.isEnabled = true
            }
            STATE_DONE -> {
                start_listener.text = "recognize micro"
                start_listener.isEnabled = true
            }
            STATE_MIC -> {
                start_listener.text = "stop micro"
                //infoView.text = "Say something"
                start_listener.isEnabled = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setErrorState(message: String) {
        start_listener.text = "recognize micro"
        start_listener.isEnabled = false
    }

    private fun checkPermissions() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequestCode
            )
        }
    }
    fun recognizeMicro() {
        if (sr != null) {
            setUiState(STATE_DONE)
            sr?.cancel()
            sr = null
        } else {
            setUiState(STATE_MIC)
            try {
                sr = SpkSpcRecognizer(model, spkModel)
                sr?.addListener(this)
                sr?.startListening()
            } catch (e: IOException) {
                e.message?.let { setErrorState(it) }
            }
        }
    }

    fun saveFile() {
        try {
            for (dirName in models.keys) {
                    val f = File(savePath, dirName)
                if (!f.exists()) {f.mkdir()}
            }
            val dir = File(savePath, modelName)
            val size = dir.listFiles()?.size ?: 0
            val fileName = (size + 1).toString() + ".txt"
            val fileToSave = File(dir, fileName)
            fileToSave.writeText(speechView.text.toString()) // it.write(speechView.text.toString())
            Toast.makeText(this@SRActivity, "Text saved to ${fileToSave.absolutePath}", Toast.LENGTH_SHORT).show()
             //writeText(speechView.text.toString())
        } catch (e:Exception) {
            Toast.makeText(this@SRActivity, "$e", Toast.LENGTH_SHORT).show()
        }
    }

}
