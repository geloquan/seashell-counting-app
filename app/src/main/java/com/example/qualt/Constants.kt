package com.example.qualt

import android.Manifest

object Constants {
    const val TAG = "cameraX"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val NUM_CLASSES = 3
    const val MODEL_FILENAME = "sgd-s-train4.tflite"
    const val ASSETS_PATH = "main/assets/$MODEL_FILENAME"
    const val MODEL_PATH = "sgd-s-train4.tflite"
}