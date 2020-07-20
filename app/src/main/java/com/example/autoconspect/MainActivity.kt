package com.example.autoconspect

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sr.*
import java.lang.Math.abs

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    lateinit var gestureDetector: GestureDetector
    var x2 = 0.0f
    var x1 = 0.0f
    var y2 = 0.0f
    var y1 = 0.0f

    companion object {
        const val MIN_DISTANCE = 60 //фикс: уменьшена дистанция

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gestureDetector = GestureDetector(this,this) // детектор свайпов

    }
    //определитель свайпов
    override fun onTouchEvent(event: MotionEvent?): Boolean { //fixme нормальные жесты
        gestureDetector.onTouchEvent(event)
         when (event?.action){
             0 -> {// это начало свайпа
                 x1= event.x
                 y1= event.x
             }
             1 -> { // это конец свайпа
                 x2=event.x
                 y2=event.y
                 val valueX: Float = kotlin.math.abs(x2 - x1)
                 val valueY: Float = kotlin.math.abs(y2 - y1)

                     if ((valueX > valueY) && (valueX >  MIN_DISTANCE))  // ось абсцисс
                     {
                         if (x1 > x2)  // вправо
                         {
                             val intent = Intent(this, Scrollingactivity::class.java) //активация правого окна и переход
                             startActivity(intent)
                         } // вправо
                         else
                         {
                             Toast.makeText(this, " LEFT SWIPE", Toast.LENGTH_SHORT).show()
                         } // свайп влево
                     }
                     if ((abs(valueY) > valueX) && (valueY  >  MIN_DISTANCE) )  //ординат
                     {


                             Toast.makeText(this, "SWIPE DOWN", Toast.LENGTH_SHORT).show()
                             startActivity(Intent(this, SRActivity::class.java))
                          //вниз
                     }

             }
         }
        return super.onTouchEvent(event)
    }


// Здесь ничего менять не нужно да и вообще оно не нужно, необходимо для реализации свайпов(жестов)
    override fun onShowPress(e: MotionEvent?) {
        //TODO("Not yet implemented") // To change body of created functions use the File | Settings | File Templates

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        //TODO("Not yet implemented") // To change body of created functions use the File | Settings | File Templates
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
       // TODO("Not yet implemented") // To change body of created functions use the File | Settings | File Templates
        return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        //TODO("Not yet implemented") // To change body of created functions use the File | Settings | File Templates
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //TODO("Not yet implemented") // To change body of created functions use the File | Settings | File Templates
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
       // TODO("Not yet implemented") // To change body of created functions use the File | Settings | File Templates

    }
}