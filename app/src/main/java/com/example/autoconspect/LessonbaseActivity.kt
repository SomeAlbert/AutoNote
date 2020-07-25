package com.example.autoconspect

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.lessonbase.*
import java.io.File
import java.lang.ref.SoftReference

class LessonbaseActivity : AppCompatActivity() {

    var savePath: File? = null
    lateinit var activityReference: SoftReference<LessonbaseActivity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lessonbase)

        activityReference = SoftReference<LessonbaseActivity>(this)
        savePath = activityReference.get()?.applicationContext?.dataDir


        val dirNames = SRActivity.models.keys
        val filesLectures = mutableMapOf<String, String>()
        try {
            for (dir in dirNames) {
                val modelFiles = File(savePath, dir).listFiles()
                if (modelFiles.isNotEmpty()) {
                    for (file in modelFiles) {
                        filesLectures["${file}_$dir"] = dir
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, " $e", Toast.LENGTH_SHORT).show()
        }


        val lecturesNames = filesLectures.keys.toMutableList()

        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lecturesNames)
        listView.adapter = mAdapter
    }
}