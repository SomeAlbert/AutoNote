package com.example.autoconspect

import android.content.Intent
import android.gesture.Gesture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_read_lesson.*
import kotlinx.android.synthetic.main.loading.*
import kotlinx.android.synthetic.main.scrollingactivity.*

class ScrollingActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scrollingactivity)
        val animation_for_transition =  loadAnimation(this, R.anim.redwaterb); // animation variable

        //transtition to english archive
        eng.setOnClickListener(){
            loading.startAnimation(animation_for_transition) //anim
            val transitiontoeng = Intent(this,MainActivity::class.java)
            startActivity(transitiontoeng)
        }
        //transtition to english archive
        rus.setOnClickListener(){
            loading.startAnimation(animation_for_transition) //anim
            val transitiontorus = Intent(this,MainActivity::class.java)
            startActivity(transitiontorus)
        }

    }
    //разберись с адрессацией кнопок




}
