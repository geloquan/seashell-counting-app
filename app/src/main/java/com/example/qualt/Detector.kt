package com.example.qualt

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class Detector(
    private val context: Context,
    private val modelPath: String,
    var detectorListener: DetectorListener
) {
    interface DetectorListener {
        fun onDetect(boundingBoxes: List<BoundingBox>, classConfidences: Map<String, Float>)
        fun onEmptyDetect()
    }

    private var interpreter: Interpreter? = null
    private val labelPath = "labels.txt"
    private var labels = mutableListOf<String>()
    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.4f
        private const val IOU_THRESHOLD = 0.5f
    }

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    fun setup() {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                numThreads = Runtime.getRuntime().availableProcessors()
            }

            interpreter = Interpreter(model, options)

            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()

            tensorWidth = inputShape?.get(1) ?: 0
            tensorHeight = inputShape?.get(2) ?: 0
            numChannel = outputShape?.get(1) ?: 0
            numElements = outputShape?.get(2) ?: 0

            loadLabels()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadLabels() {
        try {
            context.assets.open(labelPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.takeIf { it.isNotBlank() }?.let { labels.add(it) }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun detect(frame: Bitmap) {
        interpreter ?: return
        if (tensorWidth == 0) return
        if (tensorHeight == 0) return
        if (numChannel == 0) return
        if (numElements == 0) return

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)

        val tensorImage = TensorImage(INPUT_IMAGE_TYPE).apply {
            load(resizedBitmap)
        }
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter?.run(imageBuffer, output.buffer)

        val (bestBoxes, classConfidences) = bestBox(output.floatArray, frame)

        if (bestBoxes.isEmpty()) {
            detectorListener.onEmptyDetect()
            return
        }

        detectorListener.onDetect(bestBoxes, classConfidences)
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val intersectionX1 = maxOf(box1.x1, box2.x1)
        val intersectionY1 = maxOf(box1.y1, box2.y1)
        val intersectionX2 = minOf(box1.x2, box2.x2)
        val intersectionY2 = minOf(box1.y2, box2.y2)

        if (intersectionX2 < intersectionX1 || intersectionY2 < intersectionY1) {
            return 0f
        }

        val intersectionArea = (intersectionX2 - intersectionX1) * (intersectionY2 - intersectionY1)
        val box1Area = (box1.x2 - box1.x1) * (box1.y2 - box1.y1)
        val box2Area = (box2.x2 - box2.x1) * (box2.y2 - box2.y1)
        val unionArea = box1Area + box2Area - intersectionArea

        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }

    private fun findOverlappingBoxes(
        boxes: List<BoundingBox>,
        iouThreshold: Float = 0.5f
    ): List<List<BoundingBox>> {
        val groups = mutableListOf<MutableList<BoundingBox>>()
        val processedBoxes = mutableSetOf<Int>()

        for (i in boxes.indices) {
            if (i in processedBoxes) continue

            val currentGroup = mutableListOf<BoundingBox>()
            currentGroup.add(boxes[i])
            processedBoxes.add(i)

            for (j in i + 1 until boxes.size) {
                if (j in processedBoxes) continue

                val overlapsWithGroup = currentGroup.any { box ->
                    calculateIoU(box, boxes[j]) > iouThreshold
                }

                if (overlapsWithGroup) {
                    currentGroup.add(boxes[j])
                    processedBoxes.add(j)
                }
            }

            groups.add(currentGroup)
        }

        return groups
    }

    private fun selectBestBoxForGroup(group: List<BoundingBox>): BoundingBox {
        val classCount = group.groupBy { it.cls }.mapValues { it.value.size }
        val mostCommonClass = classCount.maxByOrNull { it.value }?.key ?: group[0].cls
        return group.filter { it.cls == mostCommonClass }
            .maxByOrNull { it.confidence }
            ?: group[0]
    }

    private fun bestBox(array: FloatArray, frame: Bitmap): Pair<List<BoundingBox>, Map<String, Float>> {
        val boundingBoxes = mutableListOf<BoundingBox>()
        val confidenceSums = mutableMapOf<String, Float>()
        val detectionCounts = mutableMapOf<String, Int>()

        // First pass: collect all valid bounding boxes and confidence sums
        for (c in 0 until numElements) {
            var maxConf = -1.0f
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labels[maxIdx]
                val cx = array[c]
                val cy = array[c + numElements]
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]
                val x1 = cx - (w/2F)
                val y1 = cy - (h/2F)
                val x2 = cx + (w/2F)
                val y2 = cy + (h/2F)

                if (x1 < 0F || x1 > 1F || y1 < 0F || y1 > 1F || x2 < 0F || x2 > 1F || y2 < 0F || y2 > 1F) continue

                // Update confidence sums and counts
                confidenceSums[clsName] = confidenceSums.getOrDefault(clsName, 0f) + maxConf
                detectionCounts[clsName] = detectionCounts.getOrDefault(clsName, 0) + 1

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h, confidence = maxConf,
                        cls = maxIdx, clsName = clsName
                    )
                )
            }
        }

        // Calculate average confidence for each class
        val classConfidences = confidenceSums.mapValues { (className, sum) ->
            sum / detectionCounts.getOrDefault(className, 1)
        }

        // Group overlapping boxes and select the best representative for each group
        val groups = findOverlappingBoxes(boundingBoxes)
        val finalBoxes = groups.map { group -> selectBestBoxForGroup(group) }

        return Pair(finalBoxes, classConfidences)
    }

    fun clear() {
        interpreter?.close()
        interpreter = null
    }
}