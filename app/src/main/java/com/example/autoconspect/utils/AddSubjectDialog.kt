package com.example.autoconspect.utils

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.autoconspect.MainActivity
import com.example.autoconspect.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.dialog_add_subject.*
import java.io.File
import java.io.IOException

public  class AddSubjectDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_add_subject, container)
        val modelsNames = resources.getStringArray(R.array.model_names)
        val ACTW = (rootView.findViewById(R.id.newSubjectModel) as TextInputLayout).editText as AutoCompleteTextView
        ACTW.setAdapter(activity?.let { ArrayAdapter<String>(it, R.layout.choose_model_list, modelsNames) })

        (rootView.findViewById<TextView>(R.id.title)).text = "Add new subject"

//        ACTW.setOnItemClickListener { parent, view, position, id ->
//            Toast.makeText(activity, modelsNames[position], Toast.LENGTH_SHORT).show()
//        }

        val saveNewSubject = (rootView.findViewById<Button>(R.id.save_new_sub))
        saveNewSubject.setOnClickListener {
            val newSubName = newSubjectName.editText?.text.toString()
            val newSubModel = newSubjectModel.editText?.text.toString()
            val newSubModelIndex = resources.getStringArray(R.array.model_names).indexOf(newSubModel)
            try {
                val newFilePath = File(newSubName).canonicalFile
                newSubjectName.error = null
                MainActivity.subjects.add(newSubModelIndex)
                Toast.makeText(activity, "Subject $newSubName added", Toast.LENGTH_SHORT).show()
                this.dismiss()

            } catch (e: IOException) {
                newSubjectName.error = resources.getString(R.string.wrong_sub_name)
            }
        }

        return rootView
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }
}