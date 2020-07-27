package com.example.autoconspect

import android.content.Intent
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

        val currentSubjectName = intent.getStringExtra("sub") ?: "null"
        val subjectNames =
            resources.getStringArray(R.array.subject_names) // Он берет список предметов из strings.xml/subject_names
        subjectName.text = currentSubjectName

        val dirName = SRActivity.subjects.keys.toList()[subjectNames.indexOf(currentSubjectName)]
        val filesLectures = mutableMapOf<String,String>() //{filename to filepath}
        try {
            val modelFiles = File(savePath, dirName).listFiles()
            if (modelFiles.isNotEmpty()) {
                for (file in modelFiles) {
                    filesLectures[file.name] = file.absolutePath
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, " $e", Toast.LENGTH_SHORT).show()
        }

        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filesLectures.keys.toList())
        listView.adapter = mAdapter
        listView.setOnItemClickListener { parent, view, position, id -> // fixme отрытие окна с лекцией
            val intent = Intent(this, ReadLesson::class.java)
            intent.putExtra("filepath",filesLectures[filesLectures.keys.toList()[position]])
            intent.putExtra("filename",filesLectures.keys.toList()[position])
            intent.putExtra("sub", currentSubjectName)
            startActivity(intent)

        }
    }
}