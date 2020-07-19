package com.example.autoconspect

import android.media.AudioRecord
import android.os.Handler
import android.os.Looper
import java.io.IOException
//import java.util.*
import java.util.HashSet
import kotlin.math.roundToInt

import org.kaldi.SpkModel
import org.kaldi.Model
import org.kaldi.RecognitionListener
import org.kaldi.KaldiRecognizer




open class SpkSpcRecognizer(model: Model?, spkModel: SpkModel?) {

    protected val TAG = SpkSpcRecognizer::class.java.simpleName
    private var recognizer: KaldiRecognizer? = null
    private var sampleRate = 0
    private val bufferSizeSeconds = 0.4f
    private var bufferSize = 0
    private var recorder: AudioRecord? = null
    private var recognizerThread: Thread? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners: HashSet<RecognitionListener?> = HashSet()

    init {
        recognizer = KaldiRecognizer(model, spkModel, 16000.0f)
        sampleRate = 16000
        bufferSize = (sampleRate.toFloat() * bufferSizeSeconds).roundToInt()
        recorder = AudioRecord(6, sampleRate, 16, 2, bufferSize * 2)
        if (recorder!!.state == 0) {
            recorder!!.release()
            throw IOException("Failed to initialize recorder. Microphone might be already in use.")
        }
    }

    fun addListener(listener: RecognitionListener?) {

        synchronized(listeners) { listeners.add(listener) }
    }

    fun removeListener(listener: RecognitionListener?) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    fun startListening(): Boolean {
        return if (null != recognizerThread) {
            false
        } else {
            recognizerThread = RecognizerThread()
            recognizerThread!!.start()
            true
        }
    }
    fun startListening(timeout: Int): Boolean {
        return if (null != recognizerThread) {
            false
        } else {
            recognizerThread = RecognizerThread(timeout)
            recognizerThread!!.start()
            true
        }
    }

    private fun stopRecognizerThread(): Boolean {
        return if (null == recognizerThread) {
            false
        } else {
            try {
                recognizerThread!!.interrupt()
                recognizerThread!!.join()
            } catch (var2: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            recognizerThread = null
            true
        }
    }

    fun stop(): Boolean {
        val result = stopRecognizerThread()
        if (result) {
            mainHandler.post(ResultEvent(recognizer!!.Result(), true))
        }
        return result
    }

    fun cancel(): Boolean {
        val result = stopRecognizerThread()
        recognizer!!.Result()
        return result
    }

    fun shutdown() {
        recorder!!.release()
    }

    private inner class TimeoutEvent() : RecognitionEvent() {
        protected override fun execute(listener: RecognitionListener?) {
            listener?.onTimeout()
        }
    }

    inner class OnErrorEvent constructor(private val exception: Exception): SpkSpcRecognizer.RecognitionEvent() {
//        var exception: Exception? = null

        override fun execute(listener: RecognitionListener?) {
            listener?.onError(exception)
        }
    }

    private open inner class ResultEvent(protected val hypothesis: String, private val finalResult: Boolean) :
        SpkSpcRecognizer.RecognitionEvent() {

        override fun execute(listener: RecognitionListener?) {
            if (finalResult) {
                listener?.onResult(hypothesis)
            } else {
                listener?.onPartialResult(hypothesis)
            }
        }

    }

    abstract inner class RecognitionEvent() : Runnable {
        override fun run() {
            val emptyArray = arrayOfNulls<RecognitionListener>(0)
            val var2 =
                this@SpkSpcRecognizer.listeners.toArray(emptyArray) as Array<RecognitionListener>
            val var3 = var2.size
            for (var4 in 0 until var3) {
                val listener = var2[var4]
                execute(listener)
            }
        }

        protected abstract fun execute(listener: RecognitionListener?)
    }

    private inner class RecognizerThread @JvmOverloads constructor(timeout: Int = -1) : Thread() {
        private var remainingSamples: Int
        private val timeoutSamples: Int = if (timeout != -1) {
            timeout * this@SpkSpcRecognizer.sampleRate / 1000
        } else {
            -1
        }

        override fun run() {
            this@SpkSpcRecognizer.recorder?.startRecording()
            if (this@SpkSpcRecognizer.recorder?.recordingState == 1) {
                this@SpkSpcRecognizer.recorder?.stop()
                val ioe = IOException("Failed to start recording. Microphone might be already in use.")
                this@SpkSpcRecognizer.mainHandler.post(this@SpkSpcRecognizer.OnErrorEvent(ioe))
            } else {
                val buffer = ShortArray(this@SpkSpcRecognizer.bufferSize)
                while (!interrupted() && (timeoutSamples == -1 || remainingSamples > 0)) {
                    val nread: Int = this@SpkSpcRecognizer.recorder!!.read(buffer, 0, buffer.size)
                    if (nread < 0) {
                        throw RuntimeException("error reading audio buffer")
                    }
                    val isFinal = this@SpkSpcRecognizer.recognizer?.AcceptWaveform(buffer, nread)
                    if (isFinal!!) {
                        this@SpkSpcRecognizer.mainHandler.post(
                            this@SpkSpcRecognizer.ResultEvent(
                                this@SpkSpcRecognizer.recognizer!!.Result(),
                                true
                            )
                        )
                    } else {
                        this@SpkSpcRecognizer.mainHandler.post(
                            this@SpkSpcRecognizer.ResultEvent(
                                this@SpkSpcRecognizer.recognizer!!.PartialResult(),
                                false
                            )
                        )
                    }
                    if (timeoutSamples != -1) {
                        remainingSamples -= nread
                    }
                }
                this@SpkSpcRecognizer.recorder?.stop()
                this@SpkSpcRecognizer.mainHandler.removeCallbacksAndMessages(null as Any?)
                if (timeoutSamples != -1 && remainingSamples <= 0) {
                    this@SpkSpcRecognizer.mainHandler.post(this@SpkSpcRecognizer.TimeoutEvent())
                }
            }
        }

        init {
            remainingSamples = timeoutSamples
        }
    }

}