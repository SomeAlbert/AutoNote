package com.example.autoconspect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils

import kotlinx.android.synthetic.main.scrollingactivity.*

class Scrollingactivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scrollingactivity)
        russian.setOnClickListener {
            val intent = Intent(this, LessonbaseActivity::class.java) //база кнспектов
            startActivity(intent)
        }
        eng.setOnClickListener {
            val intent = Intent(this, LessonbaseActivity::class.java) //база кнспектов
            startActivity(intent)
        }
    }
}