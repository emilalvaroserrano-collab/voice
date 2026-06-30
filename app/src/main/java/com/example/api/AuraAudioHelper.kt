package com.example.api

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object AuraAudioHelper {
    private const val TAG = "AuraAudioHelper"
    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    /**
     * Decode base64 audio and play it using Android MediaPlayer.
     */
    fun playAudioFromBase64(context: Context, base64Data: String, onComplete: () -> Unit = {}) {
        try {
            stopPlayback()
            
            val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val tempFile = File.createTempFile("aura_speech", ".wav", context.cacheDir)
            tempFile.deleteOnExit()
            
            FileOutputStream(tempFile).use { fos ->
                fos.write(audioBytes)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                    stopPlayback()
                }
                start()
            }
            Log.i(TAG, "Audio playback started.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play base64 audio", e)
            onComplete()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop media player", e)
        }
    }

    /**
     * Start recording audio using MediaRecorder.
     */
    fun startRecording(context: Context): Boolean {
        return try {
            stopRecordingAndGetBase64()
            
            outputFile = File.createTempFile("user_speech", ".wav", context.cacheDir)
            
            // Modern MediaRecorder builder/constructor
            @Suppress("DEPRECATION")
            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }
            Log.i(TAG, "Audio recording started: ${outputFile?.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            false
        }
    }

    /**
     * Stop recording and return the base64 encoded string of the recorded audio.
     */
    fun stopRecordingAndGetBase64(): String? {
        return try {
            mediaRecorder?.let {
                it.stop()
                it.release()
            }
            mediaRecorder = null
            
            val file = outputFile
            if (file != null && file.exists()) {
                val bytes = file.readBytes()
                Log.i(TAG, "Audio recording stopped, file size: ${bytes.size} bytes")
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            null
        } finally {
            mediaRecorder = null
        }
    }
}
