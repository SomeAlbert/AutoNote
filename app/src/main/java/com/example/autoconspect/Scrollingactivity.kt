package com.example.autoconspect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.scrollingactivity.*

class Scrollingactivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scrollingactivity)
        russian.setOnClickListener {

            val animation = AnimationUtils.loadAnimation(this, R.anim.etransparency)
            russian.startAnimation(animation)
            val intent = Intent(this, LessonbaseActivity::class.java) //база кнспектов
            startActivity(intent)
        }
        english2.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.etransparency)
            english2.startAnimation(animation)
            val intent = Intent(this, LessonbaseActivity::class.java) //база кнспектов
            startActivity(intent)
        }
        lesson3.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.etransparency)
            lesson3.startAnimation(animation)
            val intent = Intent(this, LessonbaseActivity::class.java) //база кнспектов
            startActivity(intent)
        }
    }
}