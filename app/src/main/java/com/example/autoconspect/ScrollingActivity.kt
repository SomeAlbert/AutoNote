package com.example.autoconspect

import android.content.Intent
import android.gesture.Gesture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.loading.*
import kotlinx.android.synthetic.main.scrollingactivity.*

class ScrollingActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scrollingactivity)

        val animation_for_transition = AnimationUtils.loadAnimation(this, R.anim.redwaterb); // animation variable

//        val subjects = SRActivity.subjects.values.toList()
        val subjectNames = resources.getStringArray(R.array.subject_names) // Он берет список предметов из strings.xml/subject_names
//
//        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, subjectNames) //fixme можно поменять android.R.layout.simple_list_item_1 на чтото более красивое. Или нет
//
//        subjectsList.adapter = mAdapter
//
//        subjectsList.setOnItemClickListener { parent, view, position, id ->
//            val intent = Intent(this, LessonbaseActivity::class.java )
//            Log.d("APPSR", position.toString())
//            intent.putExtra("sub", subjectNames[position])
//            startActivity(intent)
//        }
        eng.setOnClickListener(){
            startActivity(Intent(this, LessonbaseActivity::class.java ).putExtra("sub", subjectNames[1]))

        }
        //transtition to english archive
        rus.setOnClickListener(){
            startActivity(Intent(this, LessonbaseActivity::class.java ).putExtra("sub", subjectNames[0]))

        }



    }



}
