package com.example.autoconspect

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.autoconspect.utils.AddSubjectDialog
import kotlinx.android.synthetic.main.activity_edit_subjects.*
import kotlinx.android.synthetic.main.dialog_add_subject.*
import kotlinx.android.synthetic.main.dialog_add_subject.view.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.Path

class EditSubjects : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_subjects)

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_subject, null)
        val modelsNames = resources.getStringArray(R.array.model_names)
        val chooseModelAdapter = ArrayAdapter(this, R.layout.choose_model_list, modelsNames)
        val fragmentManager = supportFragmentManager
        val addSubjectFragment = AddSubjectDialog()
        (dialogView.newSubjectModel.editText as? AutoCompleteTextView)?.setAdapter(chooseModelAdapter)

//        val alertDialog = AlertDialog.Builder(this)
//            .setTitle("Add new subject")
//            .setView(dialogView)
//            .setPositiveButton("Add", DialogInterface.OnClickListener { dialog, which ->
//                val newSubName = newSubjectName.editText?.text.toString()
//                val newSubModel = newSubjectModel.editText?.text.toString()
//                val newSubModelIndex = resources.getStringArray(R.array.model_names).indexOf(newSubModel)
//                try {
//                    val newFilePath = File(newSubName).canonicalFile
//                    newSubjectName.error = null
//                    MainActivity.subjects.add(newSubModelIndex)
//                    Toast.makeText(this, "Subject $newSubName added", Toast.LENGTH_SHORT).show()
//
//                } catch (e: IOException) {
//                    newSubjectName.error = resources.getString(R.string.wrong_sub_name)
//                }
//
//            })
//            .setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which ->
//            })

        addSubject.setOnClickListener {
            addSubjectFragment.show(fragmentManager, "AddSubjectTAG")

        }
    }


}