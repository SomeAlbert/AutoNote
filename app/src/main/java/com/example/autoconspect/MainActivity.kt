package com.example.autoconspect


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.loading.*
import kotlinx.android.synthetic.main.second_mact_reader.*
import org.kaldi.Model
import org.kaldi.RecognitionListener
import org.kaldi.SpkModel
import java.io.File
import java.io.IOException
import java.lang.Thread.sleep
import java.lang.ref.SoftReference
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, RecognitionListener,
    AdapterView.OnItemSelectedListener {

    companion object {
        const val MIN_DISTANCE = 60 //фикс: уменьшена дистанция
        const val TAG = "AutoConspect"
        const val permissionRequestCode = 102
        const val STATE_START = 0
        const val STATE_READY = 1
        //const val STATE_DONE = 2
        const val STATE_MIC = 3
        var currentState = STATE_START
        var modelId = 0
        var subjectId = 0
        var spkModelPath = "model-spk"

        //add models here ( [model index] : model folder name )
        var models = mutableListOf(
            "model-small-ru",
            "model-android"
        )

        // ОЧЕНЬ ВАЖНО !!
        // Индекс здесь должен соответствовать индексу в strings.xml/subject_names
        //add subjects here ( [subject index] : model index )
        var subjects = mutableListOf(0, 1)

    }

    private var sr: SpkSpcRecognizer? = null
    lateinit var model: Model
    lateinit var spkModel: SpkModel
    private var gson = Gson()
    var savePath: File? = null
    var calendar = Calendar.getInstance()
    var dateFormat = SimpleDateFormat("MMMM,d", Locale.US)
    var timeFormat = SimpleDateFormat("HH:mm", Locale.US)
    lateinit var activityReference: SoftReference<MainActivity>
    lateinit var gestureDetector: GestureDetector
    var x2 = 0.0f
    var x1 = 0.0f
    var y2 = 0.0f
    var y1 = 0.0f


    init {
        System.loadLibrary("kaldi_jni")

    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animationfade = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        val animationfadein = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        val BBE1 = AnimationUtils.loadAnimation(this, R.anim.redwater);
        val BBE2 = AnimationUtils.loadAnimation(this, R.anim.redwaterb);
        val BBE = AnimationUtils.loadAnimation(this, R.anim.blackhole);


            setContentView(R.layout.activity_main)
     
            micro.startAnimation(BBE)
            circleb.startAnimation(BBE2)
            circlebg.startAnimation(BBE1)
            circles.startAnimation(BBE2)







        setUiState(STATE_START)
        start_listener.setOnClickListener {
            recognizeMicro()
        }


//        editSubjects.setOnClickListener {
//            startActivity(Intent(this, EditSubjects::class.java))
//        }
        checkPermissions()

        activityReference = SoftReference<MainActivity>(this)
        savePath = activityReference.get()?.applicationContext?.dataDir

        micro.setOnClickListener {

            main.startAnimation(animationfade)
            main.visibility = View.GONE



            setContentView(R.layout.second_mact_reader)
            setUiState(STATE_READY)
            noteView.text = resources.getStringArray(R.array.subject_names)[subjectId]

            second_mact_activity.startAnimation(animationfadein)
            val elevate = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);

            second_mact_activity.visibility = View.VISIBLE

            bottomAppBar2.setNavigationOnClickListener {
                saveFile()
            }
            floatingActionButton.apply {
                this.setOnClickListener {
                    when (currentState) {
                        STATE_READY -> {
                            //setUiState(STATE_MIC)
                            recognizeMicro()
                        }
                        STATE_MIC -> {
                            recognizeMicro()
//                            setUiState(STATE_READY)
                            this@MainActivity.sr?.cancel()
                        }
                    }

                }
                this.isEnabled = false
                setUiState(STATE_READY)
            }

            bottomAppBar2.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        //fixme search
                        true
                    }
                    R.id.archive -> {
                        val intent = Intent(
                            this,
                            ScrollingActivity::class.java
                        ) //активация правого окна и переход
                        startActivity(intent)

                        true
                    }
                    R.id.home -> {
                        this.recreate()

                        true
                    }
                    else -> {
                        false
                    }
                }
            }

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
                val valueX: Float = kotlin.math.abs(x2 - x1)

                if (valueX > MIN_DISTANCE) {
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

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

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
                currentState = STATE_START
                //infoView.text = "Preparing the recognizer"
                //speechView.setText(" ")
                //speechView.movementMethod = ScrollingMovementMethod()

            }
            STATE_READY -> {
                //infoView.text = "ready"
                currentState = STATE_READY

                start_listener.text = resources.getString(R.string.start_recognizing)
                try {
                    floatingActionButton.isEnabled = true
                    readyStateView.visibility = View.VISIBLE
                    recordingStateView.visibility = View.INVISIBLE
                } catch (e: Exception) {
                }
            }
//            STATE_DONE -> {
//                currentState = STATE_DONE
//
//                start_listener.text = resources.getString(R.string.start_recognizing)
//                floatingActionButton.isEnabled = true
//                recordingStateView.visibility = View.INVISIBLE
//                readyStateView.visibility = View.VISIBLE
//            }
            STATE_MIC -> {
                currentState = STATE_MIC

                speechView.setText("")
                start_listener.text = resources.getString(R.string.stop_recognizing)
                //infoView.text = "Say something"
                floatingActionButton.isEnabled = true
                recordingStateView.visibility = View.VISIBLE
                readyStateView.visibility = View.INVISIBLE
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
                this, arrayOf(Manifest.permission.RECORD_AUDIO), permissionRequestCode
            )
        }
    }

    fun recognizeMicro() {
        if (sr != null) {
            setUiState(
                STATE_READY
            )
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
            val subjectName = resources.getStringArray(R.array.subject_names)[subjectId]
            for (dirName in resources.getStringArray(R.array.subject_names)) {
                val pDir = File(savePath, dirName)
                if (!pDir.exists()) {
                    pDir.mkdir()
                }
            }
            val dir = File(savePath, subjectName)
            val size = dir.listFiles()?.size ?: 0
            val currentDate = dateFormat.format(calendar.time)
            val currentTime = timeFormat.format(calendar.time)
            val fileName = "$currentDate"
            val fileToSave = File(dir, fileName)
            val prevText = if (fileToSave.exists()) fileToSave.readText() else " "
            fileToSave.writeText("$prevText \n\n\t\t$currentTime\n ${speechView.text.toString()}") // it.write(speechView.text.toString())
            Toast.makeText(
                this@MainActivity,
                "Text saved to ${fileToSave.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()
            //writeText(speechView.text.toString())
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val subjectName = resources.getStringArray(R.array.subject_names).toList()[position]
        val modelName = subjects[position]
        subjectId = position
//        modelId = subjects[subjectId]
//        MaterialAlertDialogBuilder(this)
//            .setMessage(resources.getString(R.string.clear_text))
//            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
//                sr?.cancel()
//                SetupTask(this).execute()
//                recognizeMicro()
//            }
//            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
//                saveFile()
//                speechView.setText(" ")
//                sr?.cancel()
//                SetupTask(this).execute()
//                recognizeMicro()
//            }
//            .show()

    }

}







