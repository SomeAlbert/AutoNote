package com.example.autoconspect

import android.os.AsyncTask
import android.util.Log
import org.kaldi.Assets
import org.kaldi.Model
import org.kaldi.SpkModel
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

class SetupTask(activity: MainActivity) : AsyncTask<Void, Void, Exception>() {
    var activityReferenceWeak: WeakReference<MainActivity>? = null

    init {
        activityReferenceWeak = WeakReference<MainActivity>(activity)
    }

    override fun doInBackground(vararg params: Void?): Exception? {
        try {
            val assets = Assets(activityReferenceWeak?.get())
            val assetDir: File = assets.syncAssets()
            Log.d(SRActivity.TAG, "Sync files in folder: $assetDir")
            // Vosk.SetLogLevel(0)
            activityReferenceWeak?.get()?.model = Model("$assetDir/${SRActivity.models[SRActivity.modelName]}")
            activityReferenceWeak?.get()?.spkModel = SpkModel("$assetDir/${SRActivity.spkModelPath}")
        } catch (e: IOException) {
            return e
        }
        return null
    }

    override fun onPostExecute(result: Exception?) {
        if (result != null) {
            activityReferenceWeak?.get()
                ?.setErrorState("Failed to initialize the recognizer $result")
        } else run {
            activityReferenceWeak?.get()?.setUiState(SRActivity.STATE_READY)
        }
    }
}
