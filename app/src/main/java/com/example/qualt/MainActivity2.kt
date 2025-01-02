package com.example.qualt

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.qualt.databinding.ActivityLoadingScreenBinding
import com.example.qualt.databinding.ActivityMain2Binding
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity2 : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var loadingBinding: ActivityLoadingScreenBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var detector: Detector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)

        loadingBinding = ActivityLoadingScreenBinding.inflate(layoutInflater)

        setContentView(binding.root)


        detector = Detector(baseContext, Constants.MODEL_PATH, this)
        detector.setup()

        binding.imageCaptureBtnAct2.setOnClickListener {

            captureImage()
        }

        val back: ImageView = findViewById(R.id.back)
        back.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        // Switch to loading screen
        setContentView(loadingBinding.root)

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    processImage(imageProxy)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    // Return to camera view on error
                    runOnUiThread {
                        setContentView(binding.root)
                    }
                }
            }
        )
    }

    private fun processImage(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()

        detector.detect(bitmap)

        imageProxy.close()
    }


    override fun onDetect(boundingBoxes: List<BoundingBox>, classConfidences: Map<String, Float>) {
        runOnUiThread {
            val intent = Intent(this, ImageResultActivity::class.java)

            val classCounts = boundingBoxes.groupingBy { it.clsName }.eachCount()
            val classConfidences = boundingBoxes.groupBy { it.clsName }
                .mapValues { (_, boxes) -> boxes.map { it.confidence }.average().toFloat() }

            classCounts["punaw"]?.let { punawCount ->
                intent.putExtra("punaw_count", punawCount)
                intent.putExtra("punaw_conf", classConfidences["punaw"] ?: 0f)
                val confidencePercentage = ((classConfidences["punaw"] ?: 0f) * 100).toInt()
                intent.putExtra("punaw_conf_bar", confidencePercentage)
            }

            classCounts["greenshell"]?.let { greenshellCount ->
                intent.putExtra("greenshell_count", greenshellCount)
                intent.putExtra("greenshell_conf", classConfidences["greenshell"] ?: 0f)
                val confidencePercentage = ((classConfidences["greenshell"] ?: 0f) * 100).toInt()
                intent.putExtra("greenshell_conf_bar", confidencePercentage)
            }

            classCounts["tuway"]?.let { tuwayCount ->
                intent.putExtra("tuway_count", tuwayCount)
                intent.putExtra("tuway_conf", classConfidences["tuway"] ?: 0f)
                val confidencePercentage = ((classConfidences["tuway"] ?: 0f) * 100).toInt()
                intent.putExtra("tuway_conf_bar", confidencePercentage)
            }

            val notShellClasses = boundingBoxes.filterNot { it.clsName in listOf("punaw", "greenshell", "tuway") }
                .groupBy { it.clsName }
            if (notShellClasses.isNotEmpty()) {
                var totalNotShellCount = 0
                var totalConfidence = 0f

                notShellClasses.forEach { (clsName, boxes) ->
                    val count = boxes.size
                    val averageConfidence = boxes.map { it.confidence }.average().toFloat()

                    totalNotShellCount += count
                    totalConfidence += averageConfidence * count

                }

                val overallNotShellConfidence = if (totalNotShellCount > 0) totalConfidence / totalNotShellCount else 0f
                val confidencePercentage = (overallNotShellConfidence * 100).toInt()

                intent.putExtra("notshell_count", totalNotShellCount)
                intent.putExtra("notshell_conf", overallNotShellConfidence)
                intent.putExtra("notshell_conf_bar", confidencePercentage)
            }

            // Start results activity
            startActivity(intent)
            finish()
        }
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            // Handle no detection scenario
            setContentView(binding.root)
            // Optionally show a toast or dialog
            android.widget.Toast.makeText(
                this,
                "No objects detected",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Image capture use case
            imageCapture = ImageCapture.Builder().build()

            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector.clear()
    }

    companion object {
        private const val TAG = "CameraXApp"
    }
}