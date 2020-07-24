package com.example.autoconspect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import java.io.File
import kotlinx.android.synthetic.main.lessonbase.*

class LessonbaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lessonbase)

        val rootDir = Environment.getDataDirectory()
        val dirNames = SRActivity.models.keys
        val filesLectures = mapOf<String, String>()
        try {
            for (dir in dirNames) {
                val modelFiles = File(rootDir, dir).listFiles()
                for (file in modelFiles) {
                    filesLectures.plus(Pair("${file}_$dir", dir))
                }
            }
        }
        catch(e:Exception) {
            Toast.makeText(this, " $e", Toast.LENGTH_SHORT).show()
        }

        val lecturesNames = filesLectures.keys.toMutableList()
        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lecturesNames)
        listView.adapter = mAdapter
    }
}