package com.example.autoconspect


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.speechView
import kotlinx.android.synthetic.main.activity_main.spkInfo
import kotlinx.android.synthetic.main.activity_main.start_listener
import org.kaldi.Model
import org.kaldi.RecognitionListener
import org.kaldi.SpkModel
import java.io.File
import java.io.IOException
import java.lang.Math.abs
import java.lang.ref.SoftReference


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, RecognitionListener, AdapterView.OnItemSelectedListener{

    companion object {
        const val MIN_DISTANCE = 60 //фикс: уменьшена дистанция
        const val TAG = "AutoConspect"
        const val permissionRequestCode = 102
        const val STATE_START = 0
        const val STATE_READY = 1
        const val STATE_DONE = 2
        const val STATE_MIC = 3
        var modelName = "rus"
        var subjectName = "russian"
        var spkModelPath = "model-spk"

        //add models here ( {model short name} to {model folder name} )
        var models = mutableMapOf(
            "rus" to "model-small-ru",
            "eng" to "model-android"
        )

        // ОЧЕНЬ ВАЖНО !!
        // Индекс здесь должен соответствовать индексу в strings.xml/subject_names
        //add subjects here ( {subject name} to {model short name} )
        var subjects = mutableMapOf(
            "russian" to "rus",
            "english" to "eng"
        )

    }

    private var sr: SpkSpcRecognizer? = null
    lateinit var model: Model
    lateinit var spkModel: SpkModel
    private var gson = Gson()
    var savePath: File? = null
    lateinit var activityReference: SoftReference<MainActivity>
    lateinit var gestureDetector: GestureDetector
    var x2 = 0.0f
    var x1 = 0.0f
    var y2 = 0.0f
    var y1 = 0.0f


    init {
        System.loadLibrary("kaldi_jni")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUiState(STATE_START)
        start_listener.setOnClickListener {
            recognizeMicro()
        }
        saveFile.setOnClickListener {
            saveFile()
        }
        checkPermissions()

        activityReference = SoftReference<MainActivity>(this)
        savePath = activityReference.get()?.applicationContext?.dataDir

        micro.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.scale)
            micro.startAnimation(animation)
            val animation1 = AnimationUtils.loadAnimation(this, R.anim.redwater)
            circlebg.startAnimation(animation1)
            val animation2 = AnimationUtils.loadAnimation(this, R.anim.redwaterb)
            circleb.startAnimation(animation2)
            val animation3 = AnimationUtils.loadAnimation(this, R.anim.redwaterb)
            circlebg.startAnimation(animation3)
        }
        gestureDetector = GestureDetector(this, this) // детектор свайпов

        SetupTask(this).execute()

    }

    override fun onStart() {
        super.onStart()

        spinner.onItemSelectedListener = this

    }

    //определитель свайпов
    override fun onTouchEvent(event: MotionEvent?): Boolean { //fixme нормальные жесты
        gestureDetector.onTouchEvent(event)
        when (event?.action) {
            0 -> {// это начало свайпа
                x1 = event.x
                y1 = event.x
            }
            1 -> { // это конец свайпа
                x2 = event.x
                y2 = event.y
                val valueX: Float = kotlin.math.abs(x2 - x1)
                val valueY: Float = kotlin.math.abs(y2 - y1)

                if ((valueX > valueY) && (valueX > MIN_DISTANCE))  // ось абсцисс
                {
                    if (x1 > x2)  // вправо
                    {
                        val intent = Intent(
                            this,
                            ScrollingActivity::class.java
                        ) //активация правого окна и переход
                        startActivity(intent)
                    } // вправо
                    else {

                    } // свайп влево
                }
            }
        }
        return super.onTouchEvent(event)
    }


    // Здесь ничего менять не нужно да и вообще оно не нужно, необходимо для реализации свайпов(жестов)
    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {return false}

    override fun onDown(e: MotionEvent?): Boolean {return false}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float):
            Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float):
            Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {}
    override fun onPartialResult(p0: String?) {}

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
                Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sr?.cancel()
        sr?.shutdown()
    }

    override fun onError(p0: java.lang.Exception?) {
        p0?.message?.let { setErrorState(it) }
    }

    override fun onTimeout() {
        sr?.cancel()
        sr = null
        setUiState(STATE_READY)
    }

    fun setUiState(state: Int) {
        when (state) {
            STATE_START -> {
                //infoView.text = "Preparing the recognizer"
                speechView.setText(" ")
                speechView.movementMethod = ScrollingMovementMethod()
                start_listener.isEnabled = false
            }
            STATE_READY -> {
                //infoView.text = "ready"
                start_listener.text = resources.getString(R.string.start_recognizing)
                start_listener.isEnabled = true
            }
            STATE_DONE -> {
                start_listener.text = resources.getString(R.string.start_recognizing)
                start_listener.isEnabled = true
            }
            STATE_MIC -> {
                start_listener.text = resources.getString(R.string.stop_recognizing)
                //infoView.text = "Say something"
                start_listener.isEnabled = true
            }
        }
    }

    fun setErrorState(message: String) {
        start_listener.text = resources.getString(R.string.start_recognizing)
        start_listener.isEnabled = false
    }

    private fun checkPermissions() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), permissionRequestCode)
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
            for (dirName in subjects.keys) {
                val f = File(savePath, dirName)
                if (!f.exists()) {f.mkdir()}
            }
            val dir = File(savePath, subjectName)
            val size = dir.listFiles()?.size ?: 0
            val fileName = (size + 1).toString() + ".txt"
            val fileToSave = File(dir, fileName)
            fileToSave.writeText(speechView.text.toString()) // it.write(speechView.text.toString())
            Toast.makeText(this@MainActivity, "Text saved to ${fileToSave.absolutePath}", Toast.LENGTH_SHORT).show()
            //writeText(speechView.text.toString())
        } catch (e:Exception) {
            Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        subjectName = subjects.keys.toList()[position]
        modelName = subjects[subjectName].toString()
        MaterialAlertDialogBuilder(this)
            .setMessage(resources.getString(R.string.clear_text))
            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                sr?.cancel()
                SetupTask(this).execute()
                recognizeMicro()
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                saveFile()
                speechView.setText(" ")
                sr?.cancel()
                SetupTask(this).execute()
                recognizeMicro()
            }
            .show()
        // fixme должно вылезти окошко и спросить: очистить окно ввода?

    }

}