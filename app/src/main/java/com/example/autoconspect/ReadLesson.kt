package com.example.autoconspect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_read_lesson.*
import java.io.File

class ReadLesson : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_lesson)

        lessonSubject.text = intent.getStringExtra("sub")
        lessonName.text = intent.getStringExtra("filename")
        val file = File(intent.getStringExtra("filepath"))
        val lessonTextFromFile = file.bufferedReader().use { it.readText() }
        lessonText.text = lessonTextFromFile
    }
}